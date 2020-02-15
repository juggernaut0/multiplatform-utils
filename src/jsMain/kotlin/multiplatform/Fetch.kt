package multiplatform

import asynclite.await
import multiplatform.api.ApiRoute
import multiplatform.api.ApiRouteWithBody
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import kotlin.browser.window

class FetchException(message: String, val status: Short, val body: String) : Exception(message)

suspend fun fetch(method: String, path: String, body: String? = undefined, headers: Headers? = undefined): String {
    val resp = window.fetch(path, RequestInit(method = method, body = body, headers = headers)).await()
    val text = resp.text().await()
    if (!resp.ok) throw FetchException(
        "${resp.status} ${resp.statusText}",
        resp.status,
        text
    )
    return text
}

suspend fun <P, R> ApiRoute<P, R>.call(params: P, headers: Headers? = undefined, json: Json = defaultJson): R {
    return fetch(method.toString(), path.applyParams(params), headers = headers)
            .let { json.parse(responseSer, it) }
}

suspend fun <P, T, R> ApiRouteWithBody<P, T, R>.call(body: T, params: P, headers: Headers? = undefined, json: Json = defaultJson): R {
    return fetch(
        method.toString(),
        path.applyParams(params),
        body = json.stringify(requestSer, body),
        headers = headers
    ).let { json.parse(responseSer, it) }
}

private val defaultJson: Json by lazy {
    Json(JsonConfiguration.Stable.copy(strictMode = false))
}
