package de.gematik.hoba.server.plugins

import de.gematik.hoba.HobaAuthenticationProvider
import de.gematik.hoba.hoba
import io.ktor.server.sessions.*
import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureAuthentication() {

    install(Sessions) {
        cookie<HobaAuthenticationProvider.HobaCookie>("hoba_session",SessionStorageMemory()) {
        }
    }
    install(Authentication){
        hoba{
            skipWhen { call ->
                call.sessions.get<HobaAuthenticationProvider.HobaCookie>() != null
            }
        }
    }
}
