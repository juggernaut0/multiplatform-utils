package multiplatform

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.js.Date
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalDateTest {
    @Test
    fun serialize() {
        val date = LocalDate(Date(2020, 0, 1))
        val json = Json(JsonConfiguration.Stable).stringify(LocalDateSerializer, date)
        assertEquals("\"2020-01-01\"", json)
    }

    @Test
    fun deserialize() {
        val json = "\"2020-01-01\""
        val date = Json(JsonConfiguration.Stable).parse(LocalDateSerializer, json)
        assertEquals(LocalDate(Date(2020, 0, 1)), date)
    }

    @Test
    fun roundTrip() {
        val date = LocalDate(Date())
        val json = Json(JsonConfiguration.Stable)
        val jsonStr = json.stringify(LocalDateSerializer, date)
        val newDate = json.parse(LocalDateSerializer, jsonStr)
        assertEquals(date.jsDate.getFullYear(), newDate.jsDate.getFullYear())
        assertEquals(date.jsDate.getMonth(), newDate.jsDate.getMonth())
        assertEquals(date.jsDate.getDate(), newDate.jsDate.getDate())
    }
}
