package multiplatform

import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalDateTest {
    @Test
    fun serialize() {
        val date = LocalDate(2020, 1, 1)
        val json = Json.Default.encodeToString(LocalDateSerializer, date)
        assertEquals("\"2020-01-01\"", json)
    }

    @Test
    fun deserialize() {
        val json = "\"2020-01-01\""
        val date = Json.Default.decodeFromString(LocalDateSerializer, json)
        assertEquals(LocalDate(2020, 1, 1), date)
    }

    @Test
    fun roundTrip() {
        val date = Clock.System.todayAt(TimeZone.currentSystemDefault())
        val jsonStr = Json.Default.encodeToString(LocalDateSerializer, date)
        val newDate = Json.Default.decodeFromString(LocalDateSerializer, jsonStr)
        assertEquals(date.year, newDate.year)
        assertEquals(date.month, newDate.month)
        assertEquals(date.dayOfMonth, newDate.dayOfMonth)
    }
}
