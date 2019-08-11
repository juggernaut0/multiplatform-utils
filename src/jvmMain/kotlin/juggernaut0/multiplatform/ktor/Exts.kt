package juggernaut0.multiplatform.ktor

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.Principal
import io.ktor.auth.authentication
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.route
import juggernaut0.mutliplatform.api.ApiRoute
import juggernaut0.mutliplatform.api.ApiRouteWithBody
import juggernaut0.mutliplatform.api.Method
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

private suspend fun <T : Any> ApplicationCall.receiveJson(des: DeserializationStrategy<T>): T {
    return Json.parse(des, receiveText())
}

private suspend fun <T> ApplicationCall.respondJson(ser: SerializationStrategy<T>, response: T) {
    respondText(Json.stringify(ser, response), ContentType.Application.Json)
}

private fun Method.toHttpMethod() = when (this) {
    Method.GET -> HttpMethod.Get
    Method.POST -> HttpMethod.Post
    Method.PUT -> HttpMethod.Put
    Method.DELETE -> HttpMethod.Delete
}

class CallContext internal constructor(val params: Parameters, val auth: Principal?)

fun <R> Route.handleApi(apiRoute: ApiRoute<R>, handler: suspend CallContext.() -> R) {
    route(apiRoute.path.toString(), apiRoute.method.toHttpMethod()) {
        handle {
            val resp = CallContext(call.parameters, call.authentication.principal).handler()
            call.respondJson(apiRoute.responseSer, resp)
        }
    }
}

fun <T : Any, R> Route.handleApi(apiRoute: ApiRouteWithBody<T, R>, handler: suspend CallContext.(T) -> R) {
    route(apiRoute.path.toString(), apiRoute.method.toHttpMethod()) {
        handle {
            val body = call.receiveJson(apiRoute.requestSer)
            val resp = CallContext(call.parameters, call.authentication.principal).handler(body)
            call.respondJson(apiRoute.responseSer, resp)
        }
    }
}
