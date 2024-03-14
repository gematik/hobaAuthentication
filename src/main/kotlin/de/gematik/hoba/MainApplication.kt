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

package main.de.gematik.hoba

import de.gematik.hoba.server.HobaServer

fun argParser(args: Array<String>): Map<String, List<String>>
{
    var last = ""
    return args.fold(mutableMapOf()) {
            acc: MutableMap<String, MutableList<String>>, s: String ->
        acc.apply {
            if (s.startsWith('-'))
            {
                val k = s.removePrefix("-")
                this[k] = mutableListOf()
                last = k
            }
            else this[last]?.add(s)
        }
    }
}

fun main(arguments : Array<String>) {
    val args  = argParser(arguments)
    val server = HobaServer(host=args.get("host")?.first()?.toString()?:"0.0.0.0", port=args.get("port")?.first()?.toInt()?:5000)
    server.run()
    while(true) {
        Thread.sleep(1000)
    }
}