package de.gematik.hoba

import java.util.*

/**
 * Created by rk on 03.11.2022.
 * gematik.de
 */

/**
 * Represents a HOBA challenge which can be sent in the WWW-authentication header to request a signature based authentication.
 *
 * @property maxAge specifies the number of seconds from the time the HTTP response is emitted for which
 * responses to this challenge can be accepted
 * @property realm indicate the scope of protection in the manner described in HTTP/1.1, Authentication [RFC7235](https://www.rfc-editor.org/rfc/rfc7235)
 * @property challenge is an array of random bytes that the server wants the client to sign in its response. The size
 * of the byte array should be 32 bytes.
 */
class HobaAuthenticationChallenge(val maxAge:Int=10, val realm:String?=null, val challenge: ByteArray)  {

    companion object {
        /** Creates a HOBA challenge from its string representation as received in the WWW-authentication header,
         * e.g. HOBA challenge="MPl_cQSW5Aa40kGGo6haUsm4Kkzs7pQ8t4are0mzD9s=" max-age=10 realm="scuma".
         *
         * @param hobaChallenge string representation of a HOBA challende
         * @return HOBA authentication challenge or exception in case of error
          */
        fun fromString(hobaChallenge: String): HobaAuthenticationChallenge {
            val parameterMap = hobaChallenge.toParameterMap()
            require(parameterMap.containsKey("challenge") && parameterMap.containsKey("max-age")) { "malformed challenge - required parameter missing" }
            return HobaAuthenticationChallenge(parameterMap.get("max-age")!!.toInt(), parameterMap.get("realm")?.trim('"'), Base64.getUrlDecoder().decode(parameterMap.get("challenge")!!.trim('"')))
        }
    }

    /** Converts the HOBA challenge into its string representation as used in the WWW-authentication header,
     * e.g. HOBA challenge="MPl_cQSW5Aa40kGGo6haUsm4Kkzs7pQ8t4are0mzD9s=" max-age=10 realm="scuma".
     *
     * @return HOBA string representation
     */
    override fun toString(): String {
        val re = if(realm!=null)""" realm="$realm"""" else ""
        return """HOBA challenge="${Base64.getUrlEncoder().encodeToString(challenge)}" max-age=$maxAge${re}"""
    }
}