package multiplatform.graphql

import kotlinx.serialization.descriptors.SerialDescriptor
import java.util.concurrent.ConcurrentHashMap

internal actual class QueryCache {
    private val store = ConcurrentHashMap<SerialDescriptor, String>()

    actual fun get(serialDescriptor: SerialDescriptor, fn: (SerialDescriptor) -> String): String {
        return store.computeIfAbsent(serialDescriptor, fn)
    }
}
