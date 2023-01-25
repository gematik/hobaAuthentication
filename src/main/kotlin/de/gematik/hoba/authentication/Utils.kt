package de.gematik.hoba

/**
 * Created by rk on 02.11.2022.
 * gematik.de
 */

internal fun String.toParameterMap(): Map<String, String> {
    val result = mutableMapOf<String, String>()
    trim().replace("\\s+".toRegex(), " ").substringAfter(' ').split(' ').forEach {
        result.put(it.substringBefore('='), it.substringAfter('='))
    }
    return result
}

