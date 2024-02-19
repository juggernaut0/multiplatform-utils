@file:UseSerializers(UUIDSerializer::class)

package multiplatform

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
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

    @Test
    fun serializableField() {
        @Serializable
        class Wrapper(val uuid: UUID)

        val repr = """{"uuid":"5ea64f59-c945-46df-a5bf-1fd5a5e0b7d6"}"""

        val wrapper = json.decodeFromString(Wrapper.serializer(), repr)

        assertEquals(UUID("5ea64f59-c945-46df-a5bf-1fd5a5e0b7d6"), wrapper.uuid)
    }
}
