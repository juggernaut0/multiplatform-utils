package multiplatform.api

import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.builtins.serializer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse.BodyHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class ZeroDepApiClientTest {
    @Test
    fun test() {
        val client = mockk<HttpClient> {
            every {
                send(match { it.method() == "GET" && it.uri() == URI("https://test/foo") }, any<BodyHandler<String>>())
            } returns mockk {
                every { statusCode() } returns 200
                every { body() } returns "\"ok\""
            }
        }

        val apiClient = ZeroDepApiClient(client, baseUrl = "https://test")
        val route = ApiRoute(Method.GET, pathOf(Unit.serializer(), "/foo"), String.serializer())

        val resp = apiClient.callApi(route, Unit)

        assertEquals("ok", resp)
    }
}
