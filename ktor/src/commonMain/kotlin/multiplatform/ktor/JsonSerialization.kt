package multiplatform.ktor

import kotlinx.serialization.json.Json

class JsonSerialization(val json: Json) {
    class Config {
        var json: Json? = null
    }
    companion object {
        internal val defaultJson: Json by lazy {
            Json { ignoreUnknownKeys = true }
        }
    }
}