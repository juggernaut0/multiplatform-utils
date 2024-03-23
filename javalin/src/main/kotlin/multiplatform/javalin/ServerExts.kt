package multiplatform.javalin

import io.javalin.http.*
import io.javalin.plugin.ContextPlugin
import io.javalin.plugin.PluginNotRegisteredException
import io.javalin.router.JavalinDefaultRoutingApi
import io.javalin.security.RouteRole
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import multiplatform.api.ApiRoute
import multiplatform.api.ApiRouteWithBody
import multiplatform.api.Method
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class CallContext<P> internal constructor(val params: P, val auth: AuthenticationPlugin.Principal?)

fun <TApi : JavalinDefaultRoutingApi<TApi>, P, R> TApi.handleApi(apiRoute: ApiRoute<P, R>, vararg routeRoles: RouteRole, handler: CallContext<P>.() -> R): TApi {
    return addHttpHandler(apiRoute.method.toHandlerType(), apiRoute.path.pathString(), { ctx ->
        val params = try {
            apiRoute.path.extractParams(ctx.pathParamMap(), ctx.queryParamMap())
        } catch (e: SerializationException) {
            LoggerFactory.getLogger("budget").error("Failed to parse params", e)
            throw BadRequestResponse(e.message ?: HttpStatus.BAD_REQUEST.message)
        }
        val principal = ctx.maybeWith(AuthenticationPlugin::class)
        val resp = CallContext(params, principal).handler()
        ctx.respondJson(apiRoute.json, apiRoute.responseSer, resp)
    }, *routeRoles)
}

fun <TApi : JavalinDefaultRoutingApi<TApi>, P, T, R> TApi.handleApi(apiRoute: ApiRouteWithBody<P, T, R>, vararg routeRoles: RouteRole, handler: CallContext<P>.(T) -> R): TApi {
    return addHttpHandler(apiRoute.method.toHandlerType(), apiRoute.path.pathString(), { ctx ->
        val params = try {
            apiRoute.path.extractParams(ctx.pathParamMap(), ctx.queryParamMap())
        } catch (e: SerializationException) {
            LoggerFactory.getLogger("budget").error("Failed to parse params", e)
            throw BadRequestResponse(e.message ?: HttpStatus.BAD_REQUEST.message)
        }
        val principal = ctx.maybeWith(AuthenticationPlugin::class)
        val body = ctx.receiveJson(apiRoute.json, apiRoute.requestSer)
        val resp = CallContext(params, principal).handler(body)
        ctx.respondJson(apiRoute.json, apiRoute.responseSer, resp)
    }, *routeRoles)
}

private fun Method.toHandlerType(): HandlerType {
    return when (this) {
        Method.GET -> HandlerType.GET
        Method.POST -> HandlerType.POST
        Method.PUT -> HandlerType.PUT
        Method.DELETE -> HandlerType.DELETE
    }
}

private fun <T> Context.maybeWith(clazz: KClass<out ContextPlugin<*, T>>): T? {
    return try {
        with(clazz)
    } catch (e: PluginNotRegisteredException) {
        null
    }
}

private fun <T> Context.respondJson(json: Json, ser: SerializationStrategy<T>, response: T) {
    result(json.encodeToString(ser, response)).contentType(ContentType.APPLICATION_JSON)
}

private fun <T> Context.receiveJson(json: Json, des: DeserializationStrategy<T>): T {
    return try {
        json.decodeFromString(des, body())
    } catch (e: SerializationException) {
        LoggerFactory.getLogger("budget").error("Failed to parse body", e)
        throw BadRequestResponse(e.message ?: HttpStatus.BAD_REQUEST.message)
    }
}
