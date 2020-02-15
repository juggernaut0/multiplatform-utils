package multiplatform.ktor

import io.ktor.client.HttpClient
import io.ktor.client.features.feature
import io.ktor.client.request.request
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import multiplatform.api.ApiRoute
import multiplatform.api.ApiRouteWithBody
import multiplatform.api.Method

fun Method.toHttpMethod() = when (this) {
    Method.GET -> HttpMethod.Get
    Method.POST -> HttpMethod.Post
    Method.PUT -> HttpMethod.Put
    Method.DELETE -> HttpMethod.Delete
}

suspend fun <P, R> HttpClient.callApi(route: ApiRoute<P, R>, params: P, headers: Headers = Headers.Empty): R {
    val json = feature(JsonSerialization)?.json ?: JsonSerialization.defaultJson
    val resp: String = request(route.path.applyParams(params)) {
        method = route.method.toHttpMethod()
        this.headers.appendAll(headers)
    }
    return json.parse(route.responseSer, resp)
}

suspend fun <P, T, R> HttpClient.callApi(route: ApiRouteWithBody<P, T, R>, params: P, body: T, headers: Headers = Headers.Empty): R {
    val json = feature(JsonSerialization)?.json ?: JsonSerialization.defaultJson
    val resp: String = request(route.path.applyParams(params)) {
        method = route.method.toHttpMethod()
        this.body = json.stringify(route.requestSer, body)
        this.headers.appendAll(headers)
    }
    return json.parse(route.responseSer, resp)
}
