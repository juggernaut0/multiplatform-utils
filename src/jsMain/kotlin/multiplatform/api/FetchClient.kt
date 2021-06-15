package multiplatform.api

import kotlinx.serialization.json.Json
import multiplatform.fetch

class FetchClient(private val json: Json = defaultJson) : ApiClient {
    override suspend fun <P, R> callApi(
        apiRoute: ApiRoute<P, R>,
        params: P,
        headers: Headers?
    ): R {
        return fetch(
            apiRoute.method.toString(),
            apiRoute.path.applyParams(params),
            headers = headers?.toJsHeaders(),
        ).let { json.decodeFromString(apiRoute.responseSer, it) }
    }

    override suspend fun <P, T, R> callApi(
        apiRoute: ApiRouteWithBody<P, T, R>,
        params: P,
        body: T,
        headers: Headers?
    ): R {
        return fetch(
            apiRoute.method.toString(),
            apiRoute.path.applyParams(params),
            body = json.encodeToString(apiRoute.requestSer, body),
            headers = headers?.toJsHeaders(),
        ).let { json.decodeFromString(apiRoute.responseSer, it) }
    }

    companion object {
        private val defaultJson: Json by lazy {
            Json {
                ignoreUnknownKeys = true
            }
        }
    }

    private fun Headers.toJsHeaders(): org.w3c.fetch.Headers {
        if (this is FetchHeaders) return headers
        val headers = org.w3c.fetch.Headers()
        forEach { (name, value) -> headers.append(name, value) }
        return headers
    }
}

class FetchHeaders(headers: org.w3c.fetch.Headers? = undefined) : Headers {
    constructor(vararg headers: Pair<String, String>) : this(org.w3c.fetch.Headers().apply { headers.forEach { append(it.first, it.second) }})

    val headers = headers ?: org.w3c.fetch.Headers()
    
    override fun iterator(): Iterator<Pair<String, String>> {
        return iterator { 
            for (entry in headers.asDynamic().entries()) {
                yield(entry[0] to entry[1])
            }
        }
    }
}
