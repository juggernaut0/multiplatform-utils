package multiplatform.graphql

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import multiplatform.api.ApiClient
import multiplatform.api.ApiRouteWithBody
import multiplatform.api.Method
import multiplatform.api.PathTemplate

interface GraphQLQuery<T> {
    val queryString: String
    val responseDeserializer: DeserializationStrategy<T>
    val operationName: String? get() = null
    val variables: Map<String, Any?>? get() = null
    val serializersModule: SerializersModule? get() = null
}

fun <T> GraphQLQuery(
    queryString: String,
    responseDeserializer: DeserializationStrategy<T>,
    operationName: String? = null,
    variables: Map<String, JsonElement>? = null,
    serializersModule: SerializersModule? = null,
): GraphQLQuery<T> {
    return object : GraphQLQuery<T> {
        override val queryString = queryString
        override val responseDeserializer = responseDeserializer
        override val operationName = operationName
        override val variables = variables
        override val serializersModule = serializersModule
    }
}

@Serializable
class GraphQLRequest(val query: String, val operationName: String? = null, val variables: Map<String, JsonElement>? = null)

// TODO path segments can be strings or ints
@Serializable
data class GraphQLError(val message: String, val locations: List<Location> = emptyList(), val path: List<JsonElement>? = null)
@Serializable
data class Location(val line: Int, val column: Int)

@Serializable
class GraphQLResponse(val data: JsonElement? = null, val errors: List<GraphQLError> = emptyList())

class GraphQLException(val errors: List<GraphQLError>) : RuntimeException(errors.joinToString(separator = "\n"))

fun GraphQLApiRoute(path: PathTemplate<Unit>): ApiRouteWithBody<Unit, GraphQLRequest, GraphQLResponse> {
    return ApiRouteWithBody(Method.POST, path, GraphQLResponse.serializer(), GraphQLRequest.serializer())
}

suspend fun <R> ApiClient.callGraphQL(
    apiRoute: ApiRouteWithBody<Unit, GraphQLRequest, GraphQLResponse>,
    query: GraphQLQuery<R>,
): R {
    val req = GraphQLRequest(query = query.queryString, operationName = query.operationName, variables = query.variables?.mapValues { toJson(it.value) })
    return callGraphQL(apiRoute, req, query.responseDeserializer, query.serializersModule)
}

suspend fun <R> ApiClient.callGraphQL(
    apiRoute: ApiRouteWithBody<Unit, GraphQLRequest, GraphQLResponse>,
    req: GraphQLRequest,
    responseDeserializer: DeserializationStrategy<R>,
    serializersModule: SerializersModule?,
): R {
    val resp = callApi(apiRoute, Unit, req)
    if (resp.errors.isNotEmpty()) {
        throw GraphQLException(resp.errors)
    }
    val json = if (serializersModule != null) Json { this.serializersModule = serializersModule } else Json.Default
    return json.decodeFromJsonElement(responseDeserializer, resp.data!!) // data should be non-null if no errors
}

@Suppress("UNCHECKED_CAST")
internal fun toJson(e: Any?): JsonElement {
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
