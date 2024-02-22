package multiplatform.api

import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse.BodyHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class ZeroDepApiClientTest {
    @Test
    fun test() {
        @Serializable
        class Resp(val x: String)

        val client = mockk<HttpClient> {
            every {
                send(match { it.method() == "GET" && it.uri() == URI("https://test/foo") }, any<BodyHandler<String>>())
            } returns mockk {
                every { statusCode() } returns 200
                every { body() } returns "{x: ok}" // lenient json
            }
        }

        val apiClient = ZeroDepApiClient(client, baseUrl = "https://test")
        val json = Json { isLenient = true }
        val route = ApiRoute(Method.GET, pathOf(Unit.serializer(), "/foo"), Resp.serializer(), json)

        val resp = apiClient.callApi(route, Unit)

        assertEquals("ok", resp.x)
    }
}
