package multiplatform.graphql

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildQueryTest {
    @Test
    fun simple() {
        @Serializable
        class Query(val s: String, val i: Int, val b: Boolean)

        val query = GraphQLQueryBuilder.buildQuery(Query.serializer().descriptor)

        assertEquals("query{s i b}", query)
    }

    @Test
    fun nested() {
        @Serializable
        class Nested(val s: String)
        @Serializable
        class Query(val s: String, val c: Nested)

        val query = GraphQLQueryBuilder.buildQuery(Query.serializer().descriptor)

        assertEquals("query{s c{s}}", query)
    }

    @Test
    fun listPrimitives() {
        @Serializable
        class Query(val s: String, val ns: List<Int>)

        val query = GraphQLQueryBuilder.buildQuery(Query.serializer().descriptor)

        assertEquals("query{s ns}", query)
    }

    @Test
    fun listClasses() {
        @Serializable
        class Nested(val s: String)
        @Serializable
        class Query(val s: String, val cs: List<Nested>)

        val query = GraphQLQueryBuilder.buildQuery(Query.serializer().descriptor)

        assertEquals("query{s cs{s}}", query)
    }

    @Test
    fun arguments() {
        @Serializable
        class Query(@GraphQLArgument("even", "true") val ns: List<Int>)

        val query = GraphQLQueryBuilder.buildQuery(Query.serializer().descriptor)

        assertEquals("query{ns(even:true)}", query)
    }

    @Test
    fun variables() {
        @Serializable
        @GraphQLVariable("even", "Boolean!", "true")
        class Query(@GraphQLArgument("even", "\$even") val ns: List<Int>)

        val query = GraphQLQueryBuilder.buildQuery(Query.serializer().descriptor)

        assertEquals("query(\$even:Boolean!=true){ns(even:\$even)}", query)
    }
}
