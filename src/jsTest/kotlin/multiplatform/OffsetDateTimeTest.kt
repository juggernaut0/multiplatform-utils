package multiplatform

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.js.Date
import kotlin.test.Test
import kotlin.test.assertEquals

class OffsetDateTimeTest {
    @Test
    fun roundTrip() {
        val d = OffsetDateTime(Date())
        val json = Json(JsonConfiguration.Stable)
        val jsonStr = json.stringify(OffsetDateTimeSerializer, d)
        val newDate = json.parse(OffsetDateTimeSerializer, jsonStr)
        assertEquals(d, newDate)
    }
}