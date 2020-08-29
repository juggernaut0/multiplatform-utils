package multiplatform

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalDateTimeTest {
    @Test
    fun roundTrip() {
        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val jsonStr = Json.Default.encodeToString(LocalDateTimeSerializer, date)
        val newDate = Json.Default.decodeFromString(LocalDateTimeSerializer, jsonStr)
        assertEquals(date, newDate)
    }

    @Test
    fun noMillis() {
        val t = "\"2020-05-08T20:12:34\""
        val date = Json.Default.decodeFromString(LocalDateTimeSerializer, t)
        assertEquals(LocalDateTime(2020, 5, 8, 20, 12, 34), date)
    }
}
