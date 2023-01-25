package de.gematik.hoba
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.header
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlin.random.Random

class HobaAuthenticationProvider internal constructor(
    configuration: Configuration
) : AuthenticationProvider(configuration) {

    private val validateFunction = configuration.validateFunction
    private val challengeFunction = configuration.challengeFunction
    data class HobaCookie(val eoa: String)

    class Configuration internal constructor(name: String?) : Config(name) {
            internal var challengeFunction: suspend (ApplicationCall) -> Unit = { call ->
                val challenge = Random.nextBytes(32)
                val authenticationChallenge = HobaAuthenticationChallenge(challenge = challenge)
                call.response.header("WWW-Authenticate", authenticationChallenge.toString())
                call.respond(HttpStatusCode.Unauthorized)
            }
            data class HobaPrincipal(val eoa: String): Principal

            internal var validateFunction: suspend ApplicationCall.(String) -> HobaPrincipal? = { authHeader ->
                val credential = HobaAuthorizationCredential.fromString(authHeader)
                if (credential.verify("") == true ) {
                    HobaPrincipal(credential.kid.toString())
                }
                else {
                    null
                }
            }

            fun validate(body: suspend ApplicationCall.(String) -> HobaPrincipal?) {
                validateFunction = body
            }

            fun challenge(body: suspend (ApplicationCall) -> Unit) {
                challengeFunction = body
            }
        }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val authHeader = context.call.request.header("WWW-Authenticate")
        var principal: Configuration.HobaPrincipal? = null
        try{
                principal = authHeader?.let{validateFunction(context.call,it)
            }
        } catch ( ex: Exception){
            context.call.respondText("INVALID CREDENTIALS: Error message:" + ex.message, status = HttpStatusCode.Forbidden)
        }

        if (authHeader == null) {
            challengeFunction(context.call)
        } else {
            if(principal != null)
            {
                context.call.sessions.set(HobaCookie(principal.eoa))
                context.principal(principal)
            } else {
                context.call.respondText("INVALID CREDENTIALS", status = HttpStatusCode.Forbidden)
            }
        }
    }
}

fun AuthenticationConfig.hoba(
    name: String? = null,
    configure: HobaAuthenticationProvider.Configuration.() -> Unit
) {
    val provider =HobaAuthenticationProvider (HobaAuthenticationProvider.Configuration(name).apply(configure))
    register(provider)
}