package multiplatform.ktor

import io.ktor.http.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class WebApplicationException(
    val status: HttpStatusCode = HttpStatusCode.InternalServerError,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: cause?.message ?: status.toString(), cause)

class BadRequestException(message: String? = null, cause: Throwable? = null)
    : WebApplicationException(HttpStatusCode.BadRequest, message, cause)

class UnauthorizedException(message: String? = null, cause: Throwable? = null)
    : WebApplicationException(HttpStatusCode.Unauthorized, message, cause)

fun StatusPagesConfig.installWebApplicationExceptionHandler(
    logger: Logger = LoggerFactory.getLogger("ktor.application")
) {
    exception<WebApplicationException> { call, e ->
        call.respond(e.status, e.message.orEmpty())
        if (e.status.value < 500) {
            logger.warn(e.status.toString(), e)
        } else {
            logger.error(e.status.toString(), e)
        }
    }
}
