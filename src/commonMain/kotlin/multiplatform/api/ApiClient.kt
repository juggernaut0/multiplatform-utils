package multiplatform.api

interface Headers : Iterable<Pair<String, String>> {
    companion object {
        fun of(vararg headers: Pair<String, String>): Headers = DelegateHeaders(headers.asIterable())
    }

    private class DelegateHeaders(val iter: Iterable<Pair<String, String>>) : Headers {
        override fun iterator() = iter.iterator()
    }
}

interface ApiClient {
    suspend fun <P, R> callApi(apiRoute: ApiRoute<P, R>, params: P, headers: Headers? = null): R
    suspend fun <P, T, R> callApi(apiRoute: ApiRouteWithBody<P, T, R>, params: P, body: T, headers: Headers? = null): R
}

interface BlockingApiClient {
    fun <P, R> callApi(apiRoute: ApiRoute<P, R>, params: P, headers: Headers? = null): R
    fun <P, T, R> callApi(apiRoute: ApiRouteWithBody<P, T, R>, params: P, body: T, headers: Headers? = null): R
}
