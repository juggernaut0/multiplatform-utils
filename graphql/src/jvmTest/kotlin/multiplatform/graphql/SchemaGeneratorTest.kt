package multiplatform.graphql

import graphql.schema.idl.SchemaPrinter
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertTrue

class SchemaGeneratorTest {
    @Test
    fun simple() {
        @Serializable
        class Nested(val c: String)
        @Serializable
        class FParams(val x: Int)

        //val schema = toSchema(SchemaGeneratorConfig(supportedPackages = listOf()), listOf(TopLevelObject(MyQuery::class)))

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
            }
        }

        println(SchemaPrinter().print(schema))

        val result = runBlocking {
            graphQL(schema).executeSuspend(GraphQLRequest("query{a b{c d} f(x: 2)}"))
        }

        println(result.toSpecificationJson().toString())

        assertTrue(result.isDataPresent)
        assertTrue(result.errors.isEmpty())
    }
}
