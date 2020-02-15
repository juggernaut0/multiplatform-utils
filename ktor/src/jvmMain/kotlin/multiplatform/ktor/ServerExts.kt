package multiplatform.ktor

import io.ktor.application.*
import io.ktor.auth.Principal
import io.ktor.auth.authentication
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.Pipeline
import io.ktor.util.toMap
import multiplatform.api.ApiRoute
import multiplatform.api.ApiRouteWithBody
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json

object JsonSerializationFeature : ApplicationFeature<Pipeline<*, ApplicationCall>, JsonSerialization.Config, JsonSerialization> {
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
        return json.parse(des, receiveText())
    } catch (e: SerializationException) {
        throw BadRequestException(cause = e)
    }
}

private suspend fun <T> ApplicationCall.respondJson(json: Json, ser: SerializationStrategy<T>, response: T) {
    respondText(json.stringify(ser, response), ContentType.Application.Json)
}

class CallContext<P> internal constructor(val params: P, val auth: Principal?)

fun <P, R> Route.handleApi(apiRoute: ApiRoute<P, R>, handler: suspend CallContext<P>.() -> R) {
    route(apiRoute.path.toString(), apiRoute.method.toHttpMethod()) {
        handle {
            val json = call.application.featureOrNull(JsonSerializationFeature)?.json ?: JsonSerialization.defaultJson
            val params = unmapWithDefaults(json, apiRoute.path.serializer, call.parameters.toMap().mapValues { it.value.first() })
            val resp = CallContext(params, call.authentication.principal).handler()
            call.respondJson(json, apiRoute.responseSer, resp)
        }
    }
}

fun <P, T : Any, R> Route.handleApi(apiRoute: ApiRouteWithBody<P, T, R>, handler: suspend CallContext<P>.(T) -> R) {
    route(apiRoute.path.toString(), apiRoute.method.toHttpMethod()) {
        handle {
            val json = call.application.featureOrNull(JsonSerializationFeature)?.json ?: JsonSerialization.defaultJson
            val params = unmapWithDefaults(json, apiRoute.path.serializer, call.parameters.toMap().mapValues { it.value.first() })
            val body = call.receiveJson(json, apiRoute.requestSer)
            val resp = CallContext(params, call.authentication.principal).handler(body)
            call.respondJson(json, apiRoute.responseSer, resp)
        }
    }
}

// Hacky workaround to https://github.com/Kotlin/kotlinx.serialization/issues/79
private fun <T> unmapWithDefaults(json: Json, serializer: KSerializer<T>, map: Map<String, String>): T {
    // Must go all the way to string, cannot use JsonElement: https://github.com/Kotlin/kotlinx.serialization/issues/645
    val str = json.stringify((StringSerializer to StringSerializer).map, map)
    return json.parse(serializer, str)
}
