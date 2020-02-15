package multiplatform.ktor

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


class JsonSerialization(val json: Json) {
    class Config {
        var json: Json? = null
    }
    companion object : HttpClientFeature<Config, JsonSerialization> {
        override val key: AttributeKey<JsonSerialization> = AttributeKey("JsonSerialization")

        override fun install(feature: JsonSerialization, scope: HttpClient) {
            // empty
        }

        override fun prepare(block: Config.() -> Unit): JsonSerialization {
            val config = Config().also(block)
            return JsonSerialization(config.json ?: defaultJson)
        }

        internal val defaultJson: Json by lazy {
            Json(JsonConfiguration.Stable.copy(strictMode = false))
        }
    }
}