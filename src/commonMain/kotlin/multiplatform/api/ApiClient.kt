package multiplatform.api

interface Headers : Iterable<Pair<String, String>>

interface ApiClient {
    suspend fun <P, R> callApi(apiRoute: ApiRoute<P, R>, params: P, headers: Headers? = null): R
    suspend fun <P, T, R> callApi(apiRoute: ApiRouteWithBody<P, T, R>, params: P, body: T, headers: Headers? = null): R
}
