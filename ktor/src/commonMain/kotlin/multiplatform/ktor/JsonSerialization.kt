package multiplatform.ktor

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class JsonSerialization(val json: Json) {
    class Config {
        var json: Json? = null
    }
    companion object {
        internal val defaultJson: Json by lazy {
            Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true))
        }
    }
}