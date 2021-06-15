package multiplatform.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.properties.Properties

class ApiRoute<P, R>(val method: Method, val path: PathTemplate<P>, val responseSer: KSerializer<R>) {
    companion object {
        operator fun <P, T, R> invoke(method: Method, path: PathTemplate<P>, responseSer: KSerializer<R>, requestSer: KSerializer<T>) : ApiRouteWithBody<P, T, R> {
            return ApiRouteWithBody(
                method,
                path,
                responseSer,
                requestSer
            )
        }
    }
}
class ApiRouteWithBody<P, T, R>(val method: Method, val path: PathTemplate<P>, val responseSer: KSerializer<R>, val requestSer: KSerializer<T>)

enum class Method { GET, POST, PUT, DELETE }

@OptIn(ExperimentalSerializationApi::class)
class PathTemplate<P> internal constructor(
    private val serializer: KSerializer<P>,
    private val path: List<Segment>,
    private val query: List<Pair<String?, Segment>>,
    private val fragment: Segment?
) {


    fun applyParams(params: P): String {
        val paramsMap = Properties.encodeToMap(serializer, params)
        val path = "/" + path.joinToString(separator = "/") { it.apply(paramsMap) ?: "null" }
        val query = query
                .mapNotNull { (k, v) ->
                    v.apply(paramsMap)?.let { av ->
                        if (k == null) {
                            av
                        } else {
                            "$k=$av"
                        }
                    }
                }.takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = "?", separator = "&")
                .orEmpty()

        val fragment = fragment?.apply(paramsMap)?.let { "#$it" }.orEmpty()
        return path + query + fragment
    }

    fun extractParams(pathParams: Map<String, String>, rawQueryParams: Map<String, List<String>>): P {
        return Properties.decodeFromMap(serializer, pathParams + mapQueryParams(rawQueryParams))
    }

    fun pathString(): String {
        return "/" + path.joinToString(separator = "/")
    }

    // returns a map that can be fed into properties to populate P instance
    // TODO handle multiple query params with the same key
    // TODO handle keyless params
    private fun mapQueryParams(queryStringMap: Map<String, List<String>>): Map<String, String> {
        val res = mutableMapOf<String, String>()
        for ((k, vs) in queryStringMap) {
            val segment = query.find { it.first == k }?.let { it.second as? ParamSegment }
            if (segment != null) {
                res[segment.name] = vs.first()
            }
        }
        return res
    }
}

fun <P> pathOf(serializer: KSerializer<P>, uri: String): PathTemplate<P> {
    val (rawPath, rawQuery, rawFragment) = parseUri(uri)

    val path = rawPath
        .split('/')
        .filter { it.isNotBlank() }
        .map { Segment.of(it) }

    val query = rawQuery?.let { rq ->
        rq.split('&')
            .map {
                val parts = it.split('=', limit = 2)
                if (parts.size == 1) {
                    null to Segment.of(parts[0])
                } else {
                    parts[0] to Segment.of(parts[1])
                }
            }
    }.orEmpty()

    val fragment = rawFragment?.let { Segment.of(it) }

    return PathTemplate(serializer, path, query, fragment)
}

private val uriRegex = Regex("^(/?[^?#]+)(\\?[^#]*)?(#.*)?$")
private fun parseUri(uri: String): Triple<String, String?, String?> {
    val match = uriRegex.matchEntire(uri) ?: throw IllegalArgumentException("Invalid URI")
    return Triple(
            match.groupValues[1],
            match.groupValues[2].takeUnless { it.isEmpty() || it == "?" }?.substring(1),
            match.groupValues[3].takeUnless { it.isEmpty() || it == "#" }?.substring(1)
    )
}

internal sealed class Segment {
    abstract fun apply(params: Map<String, Any?>): String?

    companion object {
        private val paramMatcher = Regex("\\{(\\w+)\\}")
        fun of(str: String): Segment {
            val match = paramMatcher.matchEntire(str)
            return if (match != null) {
                ParamSegment(match.groups[1]!!.value)
            } else {
                ConstantSegment(str)
            }
        }
    }
}
private data class ConstantSegment(val seg: String) : Segment() {
    override fun apply(params: Map<String, Any?>): String = seg

    override fun toString(): String {
        return seg
    }
}
private data class ParamSegment(val name: String) : Segment() {
    override fun apply(params: Map<String, Any?>): String? = params[name]?.toString()?.urlEncode()

    override fun toString(): String {
        return "{$name}"
    }
}

internal expect fun String.urlEncode(): String
