package de.gematik.hoba

import de.gematik.hoba.HobaClient
import de.gematik.hoba.server.HobaServer
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.http.*
import de.gematik.hoba.server.plugins.configureRouting
import de.gematik.kether.crypto.AccountStore
import io.ktor.test.dispatcher.*

class HobaServerTest {
    @Test
    fun ping() = testSuspend {
        var accountStore = AccountStore.getInstance()
        val privateKey = accountStore.getAccount(AccountStore.TEST_ACCOUNT_1_R).keyPair.privateKey
        val server = HobaServer().apply { run() }
        val client = HobaClient(privateKey=privateKey)
        val res = client.ping()
        assert(res == "pong")
        server.stop()
    }
}
