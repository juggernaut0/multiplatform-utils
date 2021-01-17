package multiplatform.ktor

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.http.*
import io.ktor.util.*
import multiplatform.api.ApiRoute
import multiplatform.api.ApiRouteWithBody
import multiplatform.api.Method

fun Method.toHttpMethod() = when (this) {
    Method.GET -> HttpMethod.Get
    Method.POST -> HttpMethod.Post
    Method.PUT -> HttpMethod.Put
    Method.DELETE -> HttpMethod.Delete
}

@Deprecated(
    "Use an ApiClient",
    replaceWith = ReplaceWith(
        "KtorApiClient(this).callApi<P, R>(route, params, headers.toMultiplatformHeaders())",
    ),
)
suspend fun <P, R> HttpClient.callApi(route: ApiRoute<P, R>, params: P, headers: Headers = Headers.Empty): R {
    return KtorApiClient(this).callApi(route, params, headers.toMultiplatformHeaders())
}

@Deprecated(
    "Use an ApiClient",
    replaceWith = ReplaceWith(
        "KtorApiClient(this).callApi<P, T, R>(route, params, body, headers.toMultiplatformHeaders())",
    ),
)
suspend fun <P, T, R> HttpClient.callApi(route: ApiRouteWithBody<P, T, R>, params: P, body: T, headers: Headers = Headers.Empty): R {
    return KtorApiClient(this).callApi(route, params, body, headers.toMultiplatformHeaders())
}

fun Headers.toMultiplatformHeaders(): multiplatform.api.Headers {
    return object : multiplatform.api.Headers {
        override fun iterator(): Iterator<Pair<String, String>> {
            return flattenEntries().iterator()
        }
    }
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
