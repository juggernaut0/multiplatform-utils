package multiplatform.api

import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers

class ZeroDepApiClient(
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
    private val baseUrl: String = "",
) : BlockingApiClient {

    override fun <P, R> callApi(apiRoute: ApiRoute<P, R>, params: P, headers: Headers?): R {
        val reqBuilder = HttpRequest.newBuilder(URI(baseUrl + apiRoute.path.applyParams(params)))
            .method(apiRoute.method.name, BodyPublishers.noBody())
        if (headers != null) {
            for ((name, value) in headers) {
                reqBuilder.header(name, value)
            }
        }
        val req = reqBuilder.build()
        val resp = httpClient.send(req, BodyHandlers.ofString())
        if (resp.statusCode() > 299) {
            throw IllegalStateException("Received status code ${resp.statusCode()}: ${resp.body()}")
        }
        return apiRoute.json.decodeFromString(apiRoute.responseSer, resp.body())
    }

    override fun <P, T, R> callApi(apiRoute: ApiRouteWithBody<P, T, R>, params: P, body: T, headers: Headers?): R {
        val reqBuilder = HttpRequest.newBuilder(URI(baseUrl + apiRoute.path.applyParams(params)))
            .method(apiRoute.method.name, BodyPublishers.ofString(apiRoute.json.encodeToString(apiRoute.requestSer, body)))
        if (headers != null) {
            for ((name, value) in headers) {
                reqBuilder.header(name, value)
            }
        }
        val req = reqBuilder.build()
        val resp = httpClient.send(req, BodyHandlers.ofString())
        if (resp.statusCode() > 299) {
            throw IllegalStateException("Received status code ${resp.statusCode()}: ${resp.body()}")
        }
        return apiRoute.json.decodeFromString(apiRoute.responseSer, resp.body())
    }
}
