package multiplatform.graphql

import asynclite.async
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import multiplatform.api.*
import kotlin.test.*

class RequestTest {
    @Test
    fun simple() = async {
        val respJson = """
            {
                "data": { "greeting": "hello", "number": 3 }
            }
        """.trimIndent()
        val client = object : ApiClient {
            override suspend fun <P, R> callApi(apiRoute: ApiRoute<P, R>, params: P, headers: Headers?): R {
                fail("should not be called")
            }

            override suspend fun <P, T, R> callApi(
                apiRoute: ApiRouteWithBody<P, T, R>,
                params: P,
                body: T,
                headers: Headers?
            ): R {
                return Json.Default.decodeFromString(apiRoute.responseSer, respJson)
            }
        }
        val route = GraphQLApiRoute(pathOf(Unit.serializer(), "/"))

        @Serializable
        class Query(val greeting: String, val number: Int)

        val resp = client.callGraphQL(route, GraphQLQuery("{greeting number}", Query.serializer()))

        assertEquals("hello", resp.greeting)
        assertEquals(3, resp.number)
    }

    @Test
    fun errors() = async<Unit> {
        val respJson = """
            {
                "errors": [
                    {
                        "message": "test error",
                        "path": ["greeting"]
                    }
                ]
            }
        """.trimIndent()
        val client = object : ApiClient {
            override suspend fun <P, R> callApi(apiRoute: ApiRoute<P, R>, params: P, headers: Headers?): R {
                fail("should not be called")
            }

            override suspend fun <P, T, R> callApi(
                apiRoute: ApiRouteWithBody<P, T, R>,
                params: P,
                body: T,
                headers: Headers?
            ): R {
                return Json.Default.decodeFromString(apiRoute.responseSer, respJson)
            }
        }
        val route = GraphQLApiRoute(pathOf(Unit.serializer(), "/"))

        val exc = assertFailsWith<GraphQLException> { client.callGraphQL(route, GraphQLQuery("", Unit.serializer())) }
        assertEquals(1, exc.errors.size)
        assertEquals("test error", exc.errors[0].message)
        assertNotNull(exc.errors[0].path)
    }

    @Serializable
    class SerialDefaultTestQuery(val foo: Foo) {
        @Serializable
        sealed class Foo {
            @Serializable
            class A(val a: Int) : Foo()

            @Serializable
            object Unknown : Foo()
        }
    }

    @Test
    fun serialDefault() = async<Unit> {
        val respJson = """
            {
                "data": { "foo": { "type": "B" } }
            }
        """.trimIndent()
        val client = object : ApiClient {
            override suspend fun <P, R> callApi(apiRoute: ApiRoute<P, R>, params: P, headers: Headers?): R {
                fail("should not be called")
            }

            override suspend fun <P, T, R> callApi(
                apiRoute: ApiRouteWithBody<P, T, R>,
                params: P,
                body: T,
                headers: Headers?
            ): R {
                return Json.Default.decodeFromString(apiRoute.responseSer, respJson)
            }
        }
        val route = ApiRoute(Method.POST, pathOf(Unit.serializer(), "/"), GraphQLResponse.serializer(), GraphQLRequest.serializer())

        val module = SerializersModule {
            polymorphicDefaultDeserializer(SerialDefaultTestQuery.Foo::class) { SerialDefaultTestQuery.Foo.Unknown.serializer() }
        }
        val resp = client.callGraphQL(route, GraphQLQuery("", SerialDefaultTestQuery.serializer(), serializersModule = module))

        assertEquals(SerialDefaultTestQuery.Foo.Unknown, resp.foo)
    }
}
