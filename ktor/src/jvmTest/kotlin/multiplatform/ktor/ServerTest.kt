package multiplatform.ktor

import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.routing.routing
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import multiplatform.api.ApiRoute
import multiplatform.api.Method
import multiplatform.api.pathOf
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerTest {
    @Test
    fun testServerExts() {
        @Serializable
        data class Req(val x: String)

        val route = ApiRoute(Method.POST, pathOf(Unit.serializer(), "/test"), String.serializer(), Req.serializer())

        withTestApplication({
            install(StatusPages) {
                installWebApplicationExceptionHandler()
            }
            routing {
                handleApi(route) {
                    it.x.toUpperCase()
                }
            }
        }) {
            with(handleRequest {
                method = HttpMethod.Post
                uri = "/test"
                //language=JSON
                setBody("""{"x": "test"}""")

            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("\"TEST\"", response.content)
            }

            with(handleRequest {
                method = HttpMethod.Post
                uri = "/test"
                //language=JSON
                setBody("""{"y": "test"}""")

            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun params() {
        @Serializable
        data class Params(val path: String, val query: String)

        val route = ApiRoute(Method.GET, pathOf(Params.serializer(), "/{path}?q={query}"), String.serializer())

        withTestApplication({
            install(StatusPages) {
                installWebApplicationExceptionHandler()
            }
            routing {
                handleApi(route) {
                    params.path + params.query
                }
            }
        }) {
            with(handleRequest {
                method = HttpMethod.Get
                uri = "/test?q=foo"

            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("\"testfoo\"", response.content)
            }

            with(handleRequest {
                method = HttpMethod.Get
                uri = "/test"

            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }
}