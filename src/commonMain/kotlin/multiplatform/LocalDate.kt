package multiplatform

import kotlinx.serialization.*

expect class LocalDate

@Serializer(forClass = LocalDate::class)
object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        val (y, m, d) = value.toYMD()
        encoder.encodeString("$y-${pad(m)}-${pad(d)}")
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val (y, m, d) = try {
            decoder.decodeString().split('-', limit = 3).map { it.toInt() }
        } catch (e: NumberFormatException) {
            throw SerializationException(e.message.orEmpty(), e)
        }
        return localDateFromYMD(y, m, d)
    }

    private fun pad(i: Int): String = i.toString().padStart(2, '0')
}

// m is 1 indexed as per java standard
internal expect fun LocalDate.toYMD(): Triple<Int, Int, Int>
internal expect fun localDateFromYMD(y: Int, m: Int, d: Int): LocalDate