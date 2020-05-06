package multiplatform

import kotlin.js.Date

actual class LocalDate(val jsDate: Date) {
    override fun toString(): String {
        val (y, m, d) = toYMD()
        return "$y-$m-$d"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as LocalDate

        if (toYMD() != other.toYMD()) return false

        return true
    }

    override fun hashCode(): Int {
        return toYMD().hashCode()
    }
}

internal actual fun LocalDate.toYMD(): Triple<Int, Int, Int> {
    return Triple(jsDate.getFullYear(), jsDate.getMonth() + 1, jsDate.getDate())
}
internal actual fun localDateFromYMD(y: Int, m: Int, d: Int): LocalDate {
    return LocalDate(Date(y, m - 1, d))
}
