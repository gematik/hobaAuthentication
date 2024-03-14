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

import de.gematik.kether.abi.types.AbiAddress
import de.gematik.kether.crypto.EcdsaPrivateKey
import de.gematik.kether.crypto.EcdsaSignature
import de.gematik.kether.crypto.EllipticCurve
import de.gematik.kether.extensions.keccak
import java.util.*

/**
 * Created by rk on 03.11.2022.
 */
class HobaAuthorizationCredential(val challenge: ByteArray, val nonce: ByteArray) {

    constructor(challenge: String, nonce: ByteArray) : this(Base64.getUrlDecoder().decode(challenge), nonce)

    enum class Alg(val alg: Int){
        secp256k1(3), // ECDSA with SECP256K1 with keccak256
        secp256r1(4) // ECDSA with SECP256r1 with keccak256
    }

    val alg = Alg.secp256r1

    val curve = when(alg){
        Alg.secp256r1 -> EllipticCurve.secp256r1
        Alg.secp256k1 -> EllipticCurve.secp256k1
    }

    lateinit var kid: AbiAddress
        private set
    lateinit var signature: EcdsaSignature
        private set

    companion object {
        fun fromString(credential: String): HobaAuthorizationCredential {
            val parameterMap = credential.toParameterMap()
            require(parameterMap.containsKey("result")) { "malformed credential - parameter result missing" }
            val resultMap = parameterMap.get("result")!!.trim('"').split('.')
            require(resultMap.size == 4) { "malformed credential - invalid number of result parts: expected 4 is ${resultMap.size}" }
            return HobaAuthorizationCredential(Base64.getUrlDecoder().decode(resultMap[1]), Base64.getUrlDecoder().decode(resultMap[2])).apply {
                kid = AbiAddress(Base64.getUrlDecoder().decode(resultMap[0]))
                signature = EcdsaSignature(Base64.getUrlDecoder().decode(resultMap[3]), curve)
            }
        }
    }

    fun sign(privateKey: EcdsaPrivateKey, origin: String, realm: String? = null) {
        val publicKey = privateKey.createEcdsaPublicKey()
        kid = publicKey.toAccountAddress()
        signature = privateKey.sign(getHobaTbs(origin, realm).keccak(), publicKey)
    }

    fun verify(origin: String, realm: String? = null): Boolean {
        require(::signature.isInitialized) { "credential isn't signed" }
        val hash = (getHobaTbs(origin, realm).keccak())
        val publicKey = signature.recoverPublicKey(hash)
        return publicKey?.toAccountAddress() == kid &&  publicKey.verify(hash, signature)
    }

    override fun toString(): String {
        return """HOBA result="${Base64.getUrlEncoder().encodeToString(kid.toByteArray())}.${Base64.getUrlEncoder().encodeToString(challenge)}.${Base64.getUrlEncoder().encodeToString(nonce)}.${
            Base64.getUrlEncoder().encodeToString(signature.getEncoded())
        }""""
    }

    fun getHobaTbs(origin: String, realm: String? = null): String {
        val re = realm ?: ""
        val ch = Base64.getUrlEncoder().encodeToString(challenge)
        val nc = Base64.getUrlEncoder().encodeToString(nonce)
        val ki = Base64.getUrlEncoder().encodeToString(kid.toByteArray())
        return "${nc.length}:${nc}${alg.alg.toString().length}:${alg.alg}${origin.length}:$origin${re.length}:$re${ki.length}:${ki}${ch.length}:$ch"
    }

}