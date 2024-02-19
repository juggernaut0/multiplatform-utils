package multiplatform.ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import multiplatform.api.ApiRoute
import multiplatform.api.ApiRouteWithBody

object JsonSerializationPlugin : BaseApplicationPlugin<Pipeline<*, ApplicationCall>, JsonSerialization.Config, JsonSerialization> {
    override val key: AttributeKey<JsonSerialization> = AttributeKey("JsonSerialization")

    override fun install(
        pipeline: Pipeline<*, ApplicationCall>,
        configure: JsonSerialization.Config.() -> Unit
    ): JsonSerialization {
        val config = JsonSerialization.Config().also(configure)
        return JsonSerialization(config.json ?: JsonSerialization.defaultJson)
    }
}

private suspend fun <T : Any> ApplicationCall.receiveJson(json: Json, des: DeserializationStrategy<T>): T {
    try {
        return json.decodeFromString(des, receiveText())
    } catch (e: SerializationException) {
        throw BadRequestException(cause = e)
    }
}

private suspend fun <T> ApplicationCall.respondJson(json: Json, ser: SerializationStrategy<T>, response: T) {
    respondText(json.encodeToString(ser, response), ContentType.Application.Json)
}

class CallContext<P> internal constructor(val params: P, val auth: Principal?)

fun <P, R> Route.handleApi(apiRoute: ApiRoute<P, R>, handler: suspend CallContext<P>.() -> R) {
    route(apiRoute.path.pathString(), apiRoute.method.toHttpMethod()) {
        handle {
            val json = call.application.pluginOrNull(JsonSerializationPlugin)?.json ?: JsonSerialization.defaultJson
            val params = try {
                apiRoute.path.extractParams(call.parameters.toMap().mapValues { it.value.first() }, call.request.queryParameters.toMap())
            } catch (e: SerializationException) {
                throw BadRequestException(cause = e)
            }
            val resp = CallContext(params, call.principal()).handler()
            call.respondJson(json, apiRoute.responseSer, resp)
        }
    }
}

fun <P, T : Any, R> Route.handleApi(apiRoute: ApiRouteWithBody<P, T, R>, handler: suspend CallContext<P>.(T) -> R) {
    route(apiRoute.path.pathString(), apiRoute.method.toHttpMethod()) {
        handle {
            val json = call.application.pluginOrNull(JsonSerializationPlugin)?.json ?: JsonSerialization.defaultJson
            val params = try {
                apiRoute.path.extractParams(call.parameters.toMap().mapValues { it.value.first() }, call.request.queryParameters.toMap())
            } catch (e: SerializationException) {
                throw BadRequestException(cause = e)
            }
            val body = call.receiveJson(json, apiRoute.requestSer)
            val resp = CallContext(params, call.principal()).handler(body)
            call.respondJson(json, apiRoute.responseSer, resp)
        }
    }
}
