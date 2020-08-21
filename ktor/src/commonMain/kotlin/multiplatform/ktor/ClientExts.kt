package multiplatform.ktor

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.features.feature
import io.ktor.client.request.request
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.util.AttributeKey
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
    val json = feature(JsonSerializationClientFeature)?.json ?: JsonSerialization.defaultJson
    val resp: String = request(route.path.applyParams(params)) {
        method = route.method.toHttpMethod()
        this.headers.appendAll(headers)
    }
    return json.decodeFromString(route.responseSer, resp)
}

suspend fun <P, T, R> HttpClient.callApi(route: ApiRouteWithBody<P, T, R>, params: P, body: T, headers: Headers = Headers.Empty): R {
    val json = feature(JsonSerializationClientFeature)?.json ?: JsonSerialization.defaultJson
    val resp: String = request(route.path.applyParams(params)) {
        method = route.method.toHttpMethod()
        this.body = json.encodeToString(route.requestSer, body)
        this.headers.appendAll(headers)
    }
    return json.decodeFromString(route.responseSer, resp)
}

object JsonSerializationClientFeature : HttpClientFeature<JsonSerialization.Config, JsonSerialization> {
    override val key: AttributeKey<JsonSerialization> = AttributeKey("JsonSerialization")

    override fun install(feature: JsonSerialization, scope: HttpClient) {
        // empty
    }

    override fun prepare(block: JsonSerialization.Config.() -> Unit): JsonSerialization {
        val config = JsonSerialization.Config().also(block)
        return JsonSerialization(config.json ?: JsonSerialization.defaultJson)
    }
}
