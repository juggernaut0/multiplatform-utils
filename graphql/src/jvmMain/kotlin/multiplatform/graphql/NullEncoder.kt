package multiplatform.graphql

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
object NullEncoder : AbstractEncoder() {
    override val serializersModule: SerializersModule
        get() = SerializersModule {  }

    override fun encodeValue(value: Any) {}
}
