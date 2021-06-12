package multiplatform.ktor

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.serializer
import multiplatform.api.ApiRoute
import multiplatform.api.Method
import multiplatform.api.pathOf
import kotlin.test.Test
import kotlin.test.assertEquals

class KtorApiClientTest {
    @Test
    fun noJsonAddsDefault(): Unit = runBlocking {
        val client = KtorApiClient(HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond("42")
                }
            }
        })
        val resp = client.callApi(ApiRoute(method = Method.GET, pathOf(Unit.serializer(), "/"), Int.serializer()), Unit)
        assertEquals(42, resp)
    }
}
