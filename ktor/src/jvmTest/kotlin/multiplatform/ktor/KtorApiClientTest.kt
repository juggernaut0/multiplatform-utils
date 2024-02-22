package multiplatform.ktor

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import multiplatform.api.ApiRoute
import multiplatform.api.Method
import multiplatform.api.pathOf
import kotlin.test.Test
import kotlin.test.assertEquals

class KtorApiClientTest {
    @Test
    fun `uses route json`(): Unit = runBlocking {
        @Serializable
        class Resp(val foo: String)

        val client = KtorApiClient(HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond("{foo: bar}") // lenient json
                }
            }
        })
        val json = Json { isLenient = true }
        val route = ApiRoute(method = Method.GET, pathOf(Unit.serializer(), "/"), Resp.serializer(), json)
        val resp = client.callApi(route, Unit)
        assertEquals("bar", resp.foo)
    }
}
