package multiplatform.ktor

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

open class WebApplicationException(
    val status: HttpStatusCode = HttpStatusCode.InternalServerError,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: cause?.message ?: status.toString(), cause)

class BadRequestException(message: String? = null, cause: Throwable? = null)
    : WebApplicationException(HttpStatusCode.BadRequest, message, cause)

class UnauthorizedException(message: String? = null, cause: Throwable? = null)
    : WebApplicationException(HttpStatusCode.Unauthorized, message, cause)

fun StatusPages.Configuration.installWebApplicationExceptionHandler() {
    exception<WebApplicationException> { e ->
        call.respond(e.status, e.message.orEmpty())
        throw e
    }
}
