package juggernaut0.multiplatform

import juggernaut0.mutliplatform.api.ApiRoute
import juggernaut0.mutliplatform.api.ApiRouteWithBody
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import kotlin.browser.window

class FetchException(message: String, val status: Short, val body: String) : Exception(message)

suspend fun fetch(method: String, path: String, body: String? = undefined, headers: Headers? = undefined): String {
    val resp = window.fetch(path, RequestInit(method = method, body = body, headers = headers)).await()
    val text = resp.text().await()
    if (!resp.ok) throw FetchException("${resp.status} ${resp.statusText}", resp.status, text)
    return text
}

suspend fun <R> ApiRoute<R>.call(params: Map<String, Any?> = emptyMap(), headers: Headers? = undefined): R {
    return fetch(method.toString(), path.applyParams(params), headers = headers).let { Json.nonstrict.parse(responseSer, it) }
}

suspend fun <T, R> ApiRouteWithBody<T, R>.call(body: T, params: Map<String, Any?> = emptyMap(), headers: Headers? = undefined): R {
    @Suppress("UNCHECKED_CAST")
    val rs = requestSer
    val json = Json.stringify(rs, body)
    return fetch(method.toString(), path.applyParams(params), body = json, headers = headers)
            .let { Json.nonstrict.parse(responseSer, it) }
}
