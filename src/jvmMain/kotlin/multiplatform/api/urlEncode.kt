package multiplatform.api

import java.net.URLEncoder

internal actual fun String.urlEncode(): String {
    return URLEncoder.encode(this, Charsets.UTF_8.name())
}
