package multiplatform.graphql

import graphql.schema.idl.SchemaPrinter
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
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

        println(SchemaPrinter().print(schema))

        val response = runBlocking {
            graphQL(schema).executeSuspend(GraphQLRequest("{a b{c d e(x: 1)} f(x: 2)}"))
        }

        assertEquals("{\"a\":\"a\",\"b\":{\"c\":\"c\",\"d\":1,\"e\":3},\"f\":4}", response.data.toString())
        assertTrue(response.errors.isEmpty())
    }
}
