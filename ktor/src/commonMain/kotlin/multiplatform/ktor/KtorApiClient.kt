package multiplatform.ktor

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import multiplatform.api.ApiClient
import multiplatform.api.ApiRoute
import multiplatform.api.ApiRouteWithBody
import multiplatform.api.Headers

class KtorApiClient(private val httpClient: HttpClient = HttpClient()) : ApiClient {
    override suspend fun <P, R> callApi(apiRoute: ApiRoute<P, R>, params: P, headers: Headers?): R {
        val json = apiRoute.json
        val resp = httpClient.request(apiRoute.path.applyParams(params)) {
            method = apiRoute.method.toHttpMethod()
            headers?.forEach { (name, value) -> this.headers.append(name, value) }
        }
        return json.decodeFromString(apiRoute.responseSer, resp.bodyAsText())
    }

    override suspend fun <P, T, R> callApi(
        apiRoute: ApiRouteWithBody<P, T, R>,
        params: P,
        body: T,
        headers: Headers?
    ): R {
        val json = apiRoute.json
        val resp = httpClient.request(apiRoute.path.applyParams(params)) {
            method = apiRoute.method.toHttpMethod()
            setBody(json.encodeToString(apiRoute.requestSer, body))
            headers?.forEach { (name, value) -> this.headers.append(name, value) }
        }
        return json.decodeFromString(apiRoute.responseSer, resp.bodyAsText())
    }
}
