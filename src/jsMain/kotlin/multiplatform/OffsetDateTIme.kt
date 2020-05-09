package multiplatform

import kotlinx.serialization.SerializationException
import kotlin.js.Date
import kotlin.math.abs

actual class OffsetDateTime(val jsDate: Date) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as OffsetDateTime

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

internal actual fun OffsetDateTime.toIsoString(): String {
    return with(jsDate) {
        "${getFullYear()}-${datePad(getMonth()+1)}-${datePad(getDate())}" +
                "T${datePad(getHours())}:${datePad(getMinutes())}:${datePad(getSeconds())}.${datePad(getMilliseconds(), len = 3)}" +
                (-getTimezoneOffset()).let { tzo -> if (tzo == 0) "Z" else "${(tzo/60).toSignedString()}:${datePad(abs(tzo%60))}" }
    }
}

internal actual fun offsetDateTimeFromIsoString(s: String): OffsetDateTime {
    val d = Date(s).takeUnless { it.getTime().isNaN() } ?: throw SerializationException("Invalid date string $s")
    console.error(d.toISOString())
    return OffsetDateTime(d)
}

private fun Int.toSignedString(): String {
    val padded = datePad(abs(this))
    return if (this > 0) "+$padded" else "-$padded"
}
