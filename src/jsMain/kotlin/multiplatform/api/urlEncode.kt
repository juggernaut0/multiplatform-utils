package multiplatform.api

internal actual fun String.urlEncode(): String {
    return encodeURIComponent(this)
}

external fun encodeURIComponent(s: String): String
