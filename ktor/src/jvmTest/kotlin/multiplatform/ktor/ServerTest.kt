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
    // TODO move this back into test method once kxs supports local class serialization (kotlin 1.5.30)
    @Serializable
    data class Req(val x: String)

    @Test
    fun testServerExts() {
        val route = ApiRoute(Method.POST, pathOf(Unit.serializer(), "/test"), String.serializer(), Req.serializer())

        withTestApplication({
            install(StatusPages) {
                installWebApplicationExceptionHandler()
            }
            routing {
                handleApi(route) {
                    it.x.uppercase()
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

    // TODO move this back into test method once kxs supports local class serialization (kotlin 1.5.30)
    @Serializable
    data class Params(val path: String, val query: String)

    @Test
    fun params() {
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