package multiplatform.javalin

import io.javalin.config.JavalinConfig
import io.javalin.http.Context
import io.javalin.plugin.ContextPlugin

class AuthenticationPlugin(
    userConfig: (Config.() -> Unit)? = null
) : ContextPlugin<AuthenticationPlugin.Config, AuthenticationPlugin.Principal?>(userConfig, Config()) {
    override fun onInitialize(config: JavalinConfig) {
        config.router.mount { routing ->
            routing.beforeMatched { ctx ->
                for (provider in pluginConfig.providers) {
                    when (val result = provider.authenticate(ctx)) {
                        is Result.Authenticated -> {
                            ctx.attribute(PRINCIPAL_ATTRIBUTE, result.principal)
                            break
                        }
                        is Result.Anonymous -> continue
                        is Result.InvalidCredentials -> {
                            ctx.status(401).result("Invalid credentials")
                            ctx.skipRemainingHandlers()
                        }
                        is Result.MissingCredentials -> {
                            ctx.status(401).result("Missing credentials")
                            ctx.skipRemainingHandlers()
                        }
                    }
                }
            }
        }
    }

    override fun createExtension(context: Context): Principal? {
        return context.attribute<Principal>(PRINCIPAL_ATTRIBUTE)
    }

    class Config {
        internal val providers = mutableListOf<Provider>()

        fun register(provider: Provider) {
            providers.add(provider)
        }
    }

    interface Provider {
        fun authenticate(context: Context): Result
    }

    interface Principal

    sealed interface Result {
        data object Anonymous : Result
        data class Authenticated(val principal: Principal) : Result
        data object InvalidCredentials : Result
        data object MissingCredentials : Result
    }

    private companion object {
        private const val PRINCIPAL_ATTRIBUTE = "javalin-authentication-principal"
    }
}
