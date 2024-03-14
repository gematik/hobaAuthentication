/*
 * Copyright 2022-2024, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.hoba

import de.gematik.kether.crypto.EcdsaPrivateKey
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class HobaClient(val host: String="http://localhost", val port: Int =5000, val privateKey: EcdsaPrivateKey) {
        val client = HttpClient(CIO) {
            install(DefaultRequest)
            install(HttpCookies)
        }
        init {
        client.plugin(HttpSend).intercept { request ->
            val originalCall = execute(request)
            if (originalCall.response.status == HttpStatusCode.Forbidden)
                throw Exception("INVALID CREDENTIALS")
            if (originalCall.response.status == HttpStatusCode.Unauthorized) {
                val hobaChallenge = HobaAuthenticationChallenge.fromString(
                    originalCall.response.headers.get("WWW-Authenticate").toString()
                )
                var credential = HobaAuthorizationCredential(hobaChallenge.challenge, Random.nextBytes(32))
                credential.sign(privateKey, "")
                request.headers.set("Authorization", credential.toString())
                execute(request)
            } else {
                originalCall
            }
        }
    }
    suspend fun ping() : String? {
        var result : String? = null
        runBlocking {
            launch {
                val response = client.get("${host}:${port}/ping")
                if (response.status == HttpStatusCode.OK) {
                    result = response.bodyAsText()
                } else {
                    println(response.status)
                }
            }
        }
        return result
    }
}