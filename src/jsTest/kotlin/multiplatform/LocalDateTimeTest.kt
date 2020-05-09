package multiplatform

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.js.Date
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalDateTimeTest {
    @Test
    fun roundTrip() {
        val date = LocalDateTime(Date())
        val json = Json(JsonConfiguration.Stable)
        val jsonStr = json.stringify(LocalDateTimeSerializer, date)
        val newDate = json.parse(LocalDateTimeSerializer, jsonStr)
        assertEquals(date, newDate)
    }

    @Test
    fun noMillis() {
        val t = "\"2020-05-08T20:12:34\""
        val json = Json(JsonConfiguration.Stable)
        val date = json.parse(LocalDateTimeSerializer, t)
        assertEquals(LocalDateTime(Date(2020, 4, 8, 20, 12, 34)), date)
    }
}