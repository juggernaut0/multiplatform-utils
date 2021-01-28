package multiplatform.graphql

import graphql.ExecutionInput
import graphql.GraphQL
import graphql.execution.AsyncExecutionStrategy
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.*

fun graphQL(schema: GraphQLSchema): GraphQL = GraphQL.newGraphQL(schema).queryExecutionStrategy(AsyncExecutionStrategy()).build()

class GraphQLCoroutineContext(val coroutineScope: CoroutineScope)
val DataFetchingEnvironment.coroutineScope: CoroutineScope get() = getContext<GraphQLCoroutineContext>().coroutineScope

suspend fun GraphQL.executeSuspend(graphQLRequest: GraphQLRequest): GraphQLResponse {
    val result = coroutineScope {
        executeAsync(
            ExecutionInput
                .newExecutionInput(graphQLRequest.query)
                .operationName(graphQLRequest.operationName)
                .variables(graphQLRequest.variables)
                .context(GraphQLCoroutineContext(this))
        ).await()
    }
    val data = result.getData<Any?>()?.let { toJson(it) }
    val errors = result.errors.map { error ->
        GraphQLError(
            message = error.message,
            path = error.path.map { toJson(it) },
            locations = error.locations.map { loc -> Location(line = loc.line, column = loc.column) },
        )
    }
    return GraphQLResponse(data = data, errors = errors)
}

@Suppress("UNCHECKED_CAST")
private fun toJson(e: Any?): JsonElement {
    return when (e) {
        null -> JsonNull
        is Number -> JsonPrimitive(e)
        is Boolean -> JsonPrimitive(e)
        is String -> JsonPrimitive(e)
        is Map<*, *> -> JsonObject((e as Map<String, *>).mapValues { toJson(it.value) })
        is List<*> -> JsonArray(e.map { toJson(it) })
        else -> error("Cannot convert $e to JsonElement")
    }
}
