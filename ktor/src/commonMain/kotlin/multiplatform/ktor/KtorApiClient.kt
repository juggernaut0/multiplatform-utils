package multiplatform.ktor

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import multiplatform.api.ApiClient
import multiplatform.api.ApiRoute
import multiplatform.api.ApiRouteWithBody
import multiplatform.api.Headers

class KtorApiClient(httpClient: HttpClient = HttpClient(), json: Json? = null) : ApiClient {
    private val httpClient = httpClient.let {
        if (it.feature(JsonSerializationClientFeature) == null) {
            it.config {
                install(JsonSerializationClientFeature) { this.json = json }
            }
        } else {
            it
        }
    }

    override suspend fun <P, R> callApi(apiRoute: ApiRoute<P, R>, params: P, headers: Headers?): R {
        val json = httpClient.feature(JsonSerializationClientFeature)!!.json
        val resp: String = httpClient.request(apiRoute.path.applyParams(params)) {
            method = apiRoute.method.toHttpMethod()
            headers?.forEach { (name, value) -> this.headers.append(name, value) }
        }
        return json.decodeFromString(apiRoute.responseSer, resp)
    }

    override suspend fun <P, T, R> callApi(
        apiRoute: ApiRouteWithBody<P, T, R>,
        params: P,
        body: T,
        headers: Headers?
    ): R {
        val json = httpClient.feature(JsonSerializationClientFeature)!!.json
        val resp: String = httpClient.request(apiRoute.path.applyParams(params)) {
            method = apiRoute.method.toHttpMethod()
            this.body = json.encodeToString(apiRoute.requestSer, body)
            headers?.forEach { (name, value) -> this.headers.append(name, value) }
        }
        return json.decodeFromString(apiRoute.responseSer, resp)
    }
}
