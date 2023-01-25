package de.gematik.hoba.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import de.gematik.hoba.server.plugins.*


class HobaServer(val host: String="localhost", val port: Int=5000)
{
    val server = embeddedServer(Netty, port = port, host = host, module = Application::module)
    fun run(){
            server.start(wait = false)
    }
    fun stop(){
            server.stop()
    }
}

fun Application.module() {
    configureAuthentication()
    configureRouting()
}
