package multiplatform

import java.time.format.DateTimeFormatter

actual typealias LocalDate = java.time.LocalDate
actual typealias LocalDateTime = java.time.LocalDateTime

internal actual fun LocalDate.toYMD(): Triple<Int, Int, Int> {
    return Triple(year, monthValue, dayOfMonth)
}
internal actual fun localDateFromYMD(y: Int, m: Int, d: Int): LocalDate {
    return LocalDate.of(y, m ,d)
}

internal actual fun LocalDateTime.toIsoString(): String {
    return format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
internal actual fun localDateTimeFromIsoString(s: String): LocalDateTime {
    return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
