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
    val server = HobaServer(host=args.get("host")?.first()?.toString()?:"localhost", port=args.get("port")?.first()?.toInt()?:5000)
    server.run()
}