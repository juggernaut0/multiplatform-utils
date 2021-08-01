package multiplatform.graphql

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import multiplatform.api.ApiClient
import multiplatform.api.ApiRouteWithBody

@Serializable
class GraphQLRequest(val query: String, val operationName: String? = null, val variables: Map<String, String>? = null)

// TODO path segments can be strings or ints
@Serializable
data class GraphQLError(val message: String, val locations: List<Location> = emptyList(), val path: List<JsonElement>? = null)
@Serializable
data class Location(val line: Int, val column: Int)

@Serializable
class GraphQLResponse(val data: JsonElement? = null, val errors: List<GraphQLError> = emptyList())

class GraphQLException(val errors: List<GraphQLError>) : RuntimeException(errors.joinToString(separator = "\n"))

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@Repeatable
annotation class GraphQLArgument(val name: String, val value: String)

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.CLASS)
@Repeatable
annotation class GraphQLVariable(val name: String, val type: String, val default: String = "")

suspend fun <R> ApiClient.callGraphQL(
    apiRoute: ApiRouteWithBody<Unit, GraphQLRequest, GraphQLResponse>,
    ser: KSerializer<R>,
    operationName: String? = null,
    variables: Map<String, Any>? = null
): R {
    val query = queryCache.get(ser.descriptor) { GraphQLQueryBuilder.buildQuery(it) }
    val req = GraphQLRequest(query = query, operationName = operationName, variables = variables?.mapValues { it.value.toString() })
    val resp = callApi(apiRoute, Unit, req)
    if (resp.errors.isNotEmpty()) {
        throw GraphQLException(resp.errors)
    }
    return Json.Default.decodeFromJsonElement(ser, resp.data!!) // data should be non-null if no errors
}

private val queryCache = QueryCache()

internal expect class QueryCache() {
    fun get(serialDescriptor: SerialDescriptor, fn: (SerialDescriptor) -> String): String
}
