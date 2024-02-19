package multiplatform.javalin

import io.javalin.Javalin
import io.javalin.http.ContentType
import io.javalin.http.Context
import io.javalin.testtools.JavalinTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import multiplatform.api.ApiRoute
import multiplatform.api.Method
import multiplatform.api.pathOf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerTest {
    @Test
    fun testServerExts() {
        @Serializable
        data class Req(val x: String)

        @Serializable
        data class Params(val path: String, val query: String)

        val route = ApiRoute(Method.POST, pathOf(Params.serializer(), "/{path}?q={query}"), String.serializer(), Req.serializer())

        val app = Javalin
            .create()
            .handleApi(route) {
                it.x.uppercase() + params.path + params.query
            }

        JavalinTest.test(app) { _, client ->
            client.request("/test?q=1") {
                it.post("""{"x": "req"}""".toRequestBody(ContentType.JSON.toMediaType()))
            }.run {
                assertEquals(200, code)
                assertEquals("\"REQtest1\"", body!!.string())
            }

            with(client.post("/test")) {
                assertEquals(400, code)
            }
        }
    }

    @Test
    fun `authentication plugin`() {
        data class TestPrincipal(val name: String) : AuthenticationPlugin.Principal
        val route = ApiRoute(Method.GET, pathOf(Unit.serializer(), "/needs-auth"), String.serializer())
        val app = Javalin.create {
            it.registerPlugin(AuthenticationPlugin {
                register(object : AuthenticationPlugin.Provider {
                    override fun authenticate(context: Context): AuthenticationPlugin.Result {
                        val authHeader = context.header("Authorization")
                        return if (authHeader != null) {
                            AuthenticationPlugin.Result.Authenticated(TestPrincipal(authHeader))
                        } else {
                            AuthenticationPlugin.Result.MissingCredentials
                        }
                    }
                })
            })
        }.handleApi(route) {
            (auth as TestPrincipal).name
        }

        JavalinTest.test(app) { _, client ->
            client.get("/needs-auth") {
                it.header("Authorization", "foo")
            }.run {
                assertEquals(200, code)
                assertEquals("\"foo\"", body!!.string())
            }

            client.get("/needs-auth").run {
                assertEquals(401, code)
            }
        }
    }
}
