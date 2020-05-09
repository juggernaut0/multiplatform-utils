package multiplatform

import kotlinx.serialization.SerializationException
import kotlin.js.Date

actual class LocalDateTime(val jsDate: Date) {
    actual fun toLocalDate(): LocalDate {
        return LocalDate(jsDate)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as LocalDateTime

        if (jsDate.getTime() != other.jsDate.getTime()) return false

        return true
    }

    override fun hashCode(): Int {
        return jsDate.getTime().hashCode()
    }

    override fun toString(): String {
        return toIsoString()
    }
}

internal actual fun LocalDateTime.toIsoString(): String {
    return with(jsDate) {
        "${getFullYear()}-${datePad(getMonth()+1)}-${datePad(getDate())}" +
                "T${datePad(getHours())}:${datePad(getMinutes())}:${datePad(getSeconds())}.${datePad(getMilliseconds(), len = 3)}"
    }
}

private val localDateTimePattern = Regex("(\\d{4})-(\\d{2})-(\\d{2})[Tt](\\d{2}):(\\d{2}):(\\d{2})(?:.(\\d+))?")
internal actual fun localDateTimeFromIsoString(s: String): LocalDateTime {
    val match = localDateTimePattern.matchEntire(s) ?: throw SerializationException("Unable to parse LocalDateTime from $s")
    return LocalDateTime(Date(
        year = match.groupValues[1].toInt(),
        month = match.groupValues[2].toInt()-1,
        day = match.groupValues[3].toInt(),
        hour = match.groupValues[4].toInt(),
        minute = match.groupValues[5].toInt(),
        second = match.groupValues[6].toInt(),
        millisecond = match.groupValues[7].takeUnless { it.isEmpty() }?.toInt() ?: 0
    ))
}

internal fun datePad(i: Int, len: Int = 2): String = i.toString().padStart(len, '0')
