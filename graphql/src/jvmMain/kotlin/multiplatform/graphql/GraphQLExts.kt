package multiplatform.graphql

import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.execution.AsyncExecutionStrategy
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.*

fun graphQL(schema: GraphQLSchema): GraphQL = GraphQL.newGraphQL(schema).queryExecutionStrategy(AsyncExecutionStrategy()).build()

object CoroutineScopeContextKey
val DataFetchingEnvironment.coroutineScope: CoroutineScope get() = graphQlContext.get(CoroutineScopeContextKey)

fun GraphQL.execute(graphQLRequest: GraphQLRequest): GraphQLResponse {
    val result = execute(
        ExecutionInput
            .newExecutionInput(graphQLRequest.query)
            .operationName(graphQLRequest.operationName)
            .variables(graphQLRequest.variablesFromJson())
    )
    return completeExecution(result)
}

suspend fun GraphQL.executeSuspend(graphQLRequest: GraphQLRequest): GraphQLResponse {
    val result = coroutineScope {
        executeAsync(
            ExecutionInput
                .newExecutionInput(graphQLRequest.query)
                .operationName(graphQLRequest.operationName)
                .variables(graphQLRequest.variablesFromJson())
                .graphQLContext { it.put(CoroutineScopeContextKey, this) }
        ).await()
    }
    return completeExecution(result)
}

private fun completeExecution(result: ExecutionResult): GraphQLResponse {
    val data = result.getData<Any?>()?.let { toJson(it) }
    val errors = result.errors.map { error ->
        GraphQLError(
            message = error.message,
            path = error.path?.map { toJson(it) },
            locations = error.locations.orEmpty().map { loc -> Location(line = loc.line, column = loc.column) },
        )
    }
    return GraphQLResponse(data = data, errors = errors)
}

private fun GraphQLRequest.variablesFromJson(): Map<String, Any?> {
    val variables = variables ?: return emptyMap()
    return variables.mapValues { it.value.toObject() }
}

private fun JsonElement.toObject(): Any? {
    return when (this) {
        is JsonArray -> map { it.toObject() }
        is JsonObject -> mapValues { it.value.toObject() }
        is JsonPrimitive -> contentOrNull
    }
}
