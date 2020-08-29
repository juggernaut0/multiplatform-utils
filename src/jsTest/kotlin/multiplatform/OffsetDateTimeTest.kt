package multiplatform

import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class InstantTest {
    @Test
    fun roundTrip() {
        val d = Clock.System.now()
        val jsonStr = Json.Default.encodeToString(InstantSerializer, d)
        val newDate = Json.Default.decodeFromString(InstantSerializer, jsonStr)
        assertEquals(d, newDate)
    }
}
