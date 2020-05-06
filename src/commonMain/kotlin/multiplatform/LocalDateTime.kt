package multiplatform

import kotlinx.serialization.*

expect class LocalDateTime

internal expect fun LocalDateTime.toIsoString(): String
internal expect fun localDateTimeFromIsoString(s: String): LocalDateTime

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("LocalDateTimeSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toIsoString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return localDateTimeFromIsoString(decoder.decodeString())
    }
}
