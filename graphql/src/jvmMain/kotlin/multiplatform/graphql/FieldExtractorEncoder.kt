package multiplatform.graphql

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
class FieldExtractorEncoder(private val name: String, private val target: (Any?) -> Unit) : Encoder {
    private var done = false

    override val serializersModule: SerializersModule
        get() = SerializersModule {  }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (done) return NullEncoder
        done = true
        val targetIndex = descriptor.getElementIndex(name)
        return object : AbstractEncoder() {
            override val serializersModule: SerializersModule
                get() = SerializersModule {  }

            override fun encodeValue(value: Any) {
                target(value)
            }

            override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
                return index == targetIndex
            }
        }
    }

    override fun encodeBoolean(value: Boolean) = cantExtract()
    override fun encodeByte(value: Byte) = cantExtract()
    override fun encodeChar(value: Char) = cantExtract()
    override fun encodeDouble(value: Double) = cantExtract()
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) = cantExtract()
    override fun encodeFloat(value: Float) = cantExtract()
    override fun encodeInt(value: Int) = cantExtract()
    override fun encodeLong(value: Long) = cantExtract()
    override fun encodeNull() = cantExtract()
    override fun encodeShort(value: Short) = cantExtract()
    override fun encodeString(value: String) = cantExtract()

    private fun cantExtract(): Nothing = throw SerializationException("Can only extract fields from structures")

    private object NullEncoder : AbstractEncoder() {
        override val serializersModule: SerializersModule
            get() = SerializersModule {  }

        override fun encodeValue(value: Any) {}
    }
}
