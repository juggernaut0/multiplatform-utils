package multiplatform

import kotlinx.serialization.*

expect class OffsetDateTime

internal expect fun OffsetDateTime.toIsoString(): String
internal expect fun offsetDateTimeFromIsoString(s: String): OffsetDateTime

object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("multiplatform.OffsetDateTimeSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeString(value.toIsoString())
    }

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        return offsetDateTimeFromIsoString(decoder.decodeString())
    }
}
