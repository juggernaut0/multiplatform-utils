package multiplatform.graphql

import graphql.schema.GraphQLSchema
import graphql.schema.idl.SchemaPrinter
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SchemaGeneratorTest {
    @Test
    fun basic() {
        @Serializable
        class Nested(val c: String)
        @Serializable
        class FParams(val x: Int)

        val schema = schema {
            query {
                field("a", String.serializer()) {
                    println("a called")
                    "a"
                }
                field("b", Nested.serializer()) {
                    println("b called")
                    Nested("c")
                }
                field("f", Int.serializer(), FParams.serializer()) {
                    println("f called")
                    it.x * 2
                }
            }

            type(Nested.serializer()) {
                field("d", Int.serializer()) {
                    println("d called")
                    c.length
                }
                field("e", Int.serializer(), FParams.serializer()) {
                    println("e called")
                    it.x * 2 + c.length
                }
            }
        }

        printSchema(schema)

        val response = runBlocking {
            graphQL(schema).executeSuspend(GraphQLRequest("{a b{c d e(x: 1)} f(x: 2)}"))
        }

        assertEquals("{\"a\":\"a\",\"b\":{\"c\":\"c\",\"d\":1,\"e\":3},\"f\":4}", response.data.toString())
        assertTrue(response.errors.isEmpty())
    }

    @Test
    fun lists() {
        val schema = schema {
            query {
                field("xs", ListSerializer(Int.serializer())) {
                    listOf(1, 2, 3)
                }
            }
        }

        printSchema(schema)

        val response = runBlocking {
            graphQL(schema).executeSuspend(GraphQLRequest("{xs}"))
        }

        assertEquals("{\"xs\":[1,2,3]}", response.data.toString())
        assertTrue(response.errors.isEmpty())
    }

    @Test
    fun nullable() {
        val schema = schema {
            query {
                field("nurupo", Int.serializer().nullable) {
                    null
                }
            }
        }

        printSchema(schema)

        val response = runBlocking {
            graphQL(schema).executeSuspend(GraphQLRequest("{nurupo}"))
        }

        assertEquals("{\"nurupo\":null}", response.data.toString())
        assertTrue(response.errors.isEmpty())
    }

    @Test
    fun context() {
        @Serializable
        class Nested {
            @Transient // will not show up in schema
            var context: Int = 0
        }

        val schema = schema {
            query {
                field("a", ListSerializer(Nested.serializer())) {
                    listOf(
                        Nested().apply { context = 1 },
                        Nested().apply { context = 2 },
                        Nested().apply { context = 3 },
                    )
                }
            }

            type(Nested.serializer()) {
                field("x", Int.serializer()) {
                    context * 2
                }
            }
        }

        printSchema(schema)

        val response = runBlocking {
            graphQL(schema).executeSuspend(GraphQLRequest("{a{x}}"))
        }

        assertEquals("{\"a\":[{\"x\":2},{\"x\":4},{\"x\":6}]}", response.data.toString())
        assertTrue(response.errors.isEmpty())
    }

    private class TestContext(val value: Int) : CoroutineContext.Element {
        override val key: CoroutineContext.Key<TestContext> get() = Key
        object Key : CoroutineContext.Key<TestContext>
    }

    @Test
    fun coroutineContext() {
        val schema = schema {
            query {
                field("a", Int.serializer()) {
                    coroutineContext[TestContext.Key]!!.value
                }
            }
        }

        val response = runBlocking(context = TestContext(42)) {
            graphQL(schema).executeSuspend(GraphQLRequest("{a}"))
        }

        assertEquals("{\"a\":42}", response.data.toString())
        assertTrue(response.errors.isEmpty())
    }

    @Test
    fun variables() {
        @Serializable
        class AParams(val x: Int)

        val schema = schema {
            query {
                field("a", Int.serializer(), AParams.serializer()) {
                    it.x * 2
                }
            }
        }

        printSchema(schema)

        val response = runBlocking {
            graphQL(schema).executeSuspend(GraphQLRequest("query(\$x:Int!){a(x:\$x)}", variables = mapOf("x" to "2")))
        }

        assertEquals("{\"a\":4}", response.data.toString())
        assertTrue(response.errors.isEmpty())
    }

    @Serializable
    private sealed class Foo {
        @Serializable
        class Bar(override val foo: String, val bar: String): Foo()
        @Serializable
        class Baz(override val foo: String, val baz: String): Foo()
        abstract val foo: String
    }

    @Test
    fun interfaces() {
        val schema = schema {
            query {
                field("foo", Foo.serializer()) {
                    Foo.Bar("bar's foo", "bar")
                }
            }

            `interface`(Foo.serializer())
        }

        printSchema(schema)

        val response = runBlocking {
            graphQL(schema).executeSuspend(GraphQLRequest("{foo{foo ... on Bar {bar}}}"))
        }

        response.errors.forEach { println(it) }

        assertEquals("{\"foo\":{\"foo\":\"bar's foo\",\"bar\":\"bar\"}}", response.data.toString())
        assertTrue(response.errors.isEmpty())
    }

    private fun printSchema(schema: GraphQLSchema) {
        println(SchemaPrinter(SchemaPrinter.Options.defaultOptions().includeDirectives(false)).print(schema))
    }
}
