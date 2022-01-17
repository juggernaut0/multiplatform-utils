package multiplatform

import asynclite.await
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import multiplatform.api.ApiRoute
import multiplatform.api.ApiRouteWithBody
import multiplatform.api.FetchClient
import multiplatform.api.FetchHeaders
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise

class FetchException(message: String, val status: Short, val body: String) : Exception(message)

suspend fun fetch(method: String, path: String, body: String? = undefined, headers: Headers? = undefined): String {
    return fetch(window::fetch, method, path, body, headers)
}
internal typealias Fetcher = (dynamic, RequestInit) -> Promise<Response>
internal suspend fun fetch(fetcher: Fetcher, method: String, path: String, body: String?, headers: Headers?): String {
    val init = RequestInit(method = method)
    // Setting these to undefined is fine, but can't set them to null
    if (body != null) {
        init.body = body
    }
    if (headers != null) {
        init.headers = headers
    }
    val resp = fetcher(path, init).await()
    val text = resp.text().await()
    if (!resp.ok) throw FetchException(
        "${resp.status} ${resp.statusText}",
        resp.status,
        text
    )
    return text
}

@Deprecated(
    message = "Configure and use a client",
    replaceWith = ReplaceWith("FetchClient(json).callApi<P, R>(this, params, FetchHeaders(headers))",
        imports = ["multiplatform.api.FetchClient", "multiplatform.api.FetchHeaders"])
)
suspend fun <P, R> ApiRoute<P, R>.call(params: P, headers: Headers? = undefined, json: Json = defaultJson): R {
    return FetchClient(json).callApi(this, params, FetchHeaders(headers))
}

@Deprecated(
    message = "Configure and use a client",
    replaceWith = ReplaceWith("FetchClient(json).callApi<P, T, R>(this, params, body, FetchHeaders(headers))",
        imports = ["multiplatform.api.FetchClient"])
)
suspend fun <P, T, R> ApiRouteWithBody<P, T, R>.call(body: T, params: P, headers: Headers? = undefined, json: Json = defaultJson): R {
    return FetchClient(json).callApi(this, params, body, FetchHeaders(headers))
}

private val defaultJson: Json by lazy {
    Json {
        ignoreUnknownKeys = true
    }
}
