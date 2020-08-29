package multiplatform

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class UUIDTest {
    private val json = Json.Default

    @Test
    fun serialize() {
        val repr = "5ea64f59-c945-46df-a5bf-1fd5a5e0b7d6"
        val uuid = UUID(repr)

        assertEquals("\"$repr\"", json.encodeToString(UUIDSerializer, uuid))
    }

    @Test
    fun deserialize() {
        val repr = "5ea64f59-c945-46df-a5bf-1fd5a5e0b7d6"

        val uuid = json.decodeFromString(UUIDSerializer, "\"$repr\"")

        assertEquals(UUID(repr), uuid)
    }
}