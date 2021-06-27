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

        assertEquals("{s i b}", query)
    }

    @Test
    fun nested() {
        @Serializable
        class Nested(val s: String)
        @Serializable
        class Query(val s: String, val c: Nested)

        val query = GraphQLQueryBuilder.buildQuery(Query.serializer().descriptor)

        assertEquals("{s c{s}}", query)
    }

    @Test
    fun listPrimitives() {
        @Serializable
        class Query(val s: String, val ns: List<Int>)

        val query = GraphQLQueryBuilder.buildQuery(Query.serializer().descriptor)

        assertEquals("{s ns}", query)
    }

    @Test
    fun listClasses() {
        @Serializable
        class Nested(val s: String)
        @Serializable
        class Query(val s: String, val cs: List<Nested>)

        val query = GraphQLQueryBuilder.buildQuery(Query.serializer().descriptor)

        assertEquals("{s cs{s}}", query)
    }

    @Test
    fun arguments() {
        @Serializable
        class Query(@GraphQLArgument("even", "true") val ns: List<Int>)

        val query = GraphQLQueryBuilder.buildQuery(Query.serializer().descriptor)

        assertEquals("{ns(even:true)}", query)
    }

    @Test
    fun variables() {
        @Serializable
        @GraphQLVariable("even", "Boolean!", "true")
        class Query(@GraphQLArgument("even", "\$even") val ns: List<Int>)

        val query = GraphQLQueryBuilder.buildQuery(Query.serializer().descriptor)

        assertEquals("query(\$even:Boolean!=true){ns(even:\$even)}", query)
    }

    @Serializable
    class InlineFragments(val foo: Foo) {
        @Serializable
        sealed class Foo {
            @Serializable
            class A(val a: Int) : Foo()

            @Serializable
            class B(val b: String) : Foo()
        }
    }

    @Test
    fun inlineFragments() {
        val query = GraphQLQueryBuilder.buildQuery(InlineFragments.serializer().descriptor)

        assertEquals("{foo{type:__typename...on A{a}...on B{b}}}", query)
    }

    @Serializable
    class EmptyCase(val foo: Foo) {
        @Serializable
        sealed class Foo {
            @Serializable
            class A(val a: Int) : Foo()

            @Serializable
            object B : Foo()
        }
    }

    @Test
    fun emptyCase() {
        val query = GraphQLQueryBuilder.buildQuery(EmptyCase.serializer().descriptor)

        assertEquals("{foo{type:__typename...on A{a}}}", query)
    }
}
