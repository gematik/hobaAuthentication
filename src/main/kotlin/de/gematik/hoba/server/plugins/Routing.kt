package de.gematik.hoba.server.plugins

import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*

fun Application.configureRouting() {
    routing{
    authenticate {
        get("/ping") {
            call.respondText("pong")
        }
    }
    }
}
