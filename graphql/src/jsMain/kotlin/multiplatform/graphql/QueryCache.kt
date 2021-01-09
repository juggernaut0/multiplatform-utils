package multiplatform.graphql

import kotlinx.serialization.descriptors.SerialDescriptor
import multiplatform.api.ApiRouteWithBody

internal actual class QueryCache {
    private val store = mutableMapOf<SerialDescriptor, String>()

    actual fun get(serialDescriptor: SerialDescriptor, fn: (SerialDescriptor) -> String): String {
        return store.getOrPut(serialDescriptor) { fn(serialDescriptor) }
    }
}

