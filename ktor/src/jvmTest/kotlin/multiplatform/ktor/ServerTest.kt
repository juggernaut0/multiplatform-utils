package multiplatform.ktor

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
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

        testApplication {
            install(StatusPages) {
                installWebApplicationExceptionHandler()
            }
            routing {
                handleApi(route) {
                    it.x.uppercase()
                }
            }

            with(client.post {
                url("/test")
                //language=JSON
                setBody("""{"x": "test"}""")
            }) {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("\"TEST\"", bodyAsText())
            }

            with(client.post {
                url("/test")
                //language=JSON
                setBody("""{"y": "test"}""")

            }) {
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }
    }

    @Test
    fun params() {
        @Serializable
        data class Params(val path: String, val query: String)

        val route = ApiRoute(Method.GET, pathOf(Params.serializer(), "/{path}?q={query}"), String.serializer())

        testApplication {
            install(StatusPages) {
                installWebApplicationExceptionHandler()
            }
            routing {
                handleApi(route) {
                    params.path + params.query
                }
            }

            with(client.get {
                method = HttpMethod.Get
                url("/test?q=foo")
            }) {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("\"testfoo\"", bodyAsText())
            }

            with(client.get {
                method = HttpMethod.Get
                url("/test")
            }) {
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }
    }
}