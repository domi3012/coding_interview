import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

val uni = "https://ufind.univie.ac.at/de/"
val year = Calendar.getInstance().get(Calendar.YEAR)
var connection: Connection? = null


fun getTimeLines(mapping: Map<String, String>): MutableMap<String, LocalDateTime> {
    var subjects = mutableMapOf<String, LocalDateTime>()
    var UIcounter = 0
    mapping.forEach { key, value ->
        var document: Document?
        try {
            connection!!.url("$uni$key")
            document = connection!!.get()
            print(". ")
            UIcounter++;
            if (UIcounter % 40 == 0) println()
            val statusCode = connection!!.response().statusCode()
            if (statusCode != 200) throw Exception("error with connection")
        } catch (e: IOException) {
            e.printStackTrace()
            return@forEach
        }
        //TODO check when null
        val mappingLV = document!!.select(".usse-id-group")
        //TODO extract dates from string maybe regex?
        val tmp = mappingLV.select(".eventinfo")
        val eventList = mappingLV.select(".events")
        var date = mutableListOf<String>()
        var hour = mutableListOf<String>()
        if (tmp.size != 0) {
            val regex = Regex("(\\d{2}.\\d{2}.\\d{4},?[^)])")
            val regex2 = Regex("([0-2]?[0-9].?:?[0-6][0-9]-)")
            val matches = regex.findAll(tmp.text())
            val matches2 = regex2.findAll(tmp.text())

            matches.forEach { matched ->
                val tmp = matched.groups[0]!!.value.toString()
                var indexOf = tmp.indexOf(',')
                var length = if (indexOf != -1) indexOf else tmp.length - 1
                date.add(tmp.substring(0, length))
            }
            matches2.forEach { matched ->
                val tmp = matched.groups[0]!!.value
                hour.add(tmp.toString().replace('.', ':').replace(' ', ':').substring(0, tmp.toString().length - 1))
            }
        } else if (eventList.size != 0) {

            eventList.select(".date").forEach { elem ->
                var tmp = elem.text()
                if (tmp.length < 7) tmp += "$year"
                date.add(tmp)
            }
            eventList.select(".time").forEach { elem ->
                val tmp = elem.text().replace('.', ':')
                if (tmp.length != 0) hour.add(tmp.substring(0, tmp.length - 8))
            }
        }
        var counter = 0
        date.zip(hour).forEach { pair ->
            if (pair.first.length == 0 || pair.second.length == 0) {
                return@forEach
            }
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy H:m")
            try {
                val dateTime = LocalDateTime.parse("${pair.first} ${pair.second}", formatter)
                subjects.put("$value$counter", dateTime)
                counter++
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }
    }
    println()
    return subjects
}

fun getAllLV(url: String): MutableMap<String, LocalDateTime> {
    var document: Document? = null
    var mappingValues = mutableMapOf<String, String>()
    try {
        connection!!.url("$uni$url")
        document = connection!!.get()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    var mappingLV = document!!.select(".what")
    mappingLV.forEach { mappingValues.put(it.attr("href"), it.text()) }
    mappingValues.remove(mappingValues.keys.last())
    return getTimeLines(mappingValues)
}


fun main() {
    var mapping = mutableMapOf<String, String>()
    var document: Document? = null
    try {
        connection = Jsoup.connect("https://ufind.univie.ac.at/de/vvz.html").userAgent("Opera").timeout(10*100000)
        document = connection!!.get()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    val mappingHref = document!!.select(".link")
    mappingHref.forEach { mapping.put(it.text(), it.attr("href"))}
    mapping.remove(mapping.keys.first())

    while (true) {
        println("Zur Zeit gibt es diese Studienrichtungen")
        mapping.forEach { it -> println(it.key) }
        println("Wähle dein Studium aus")
        var input: String? = ""
        while (input == "") {
            input = readLine()
            if (input == "exit") {
                break
            }
        }
        var subject: String = ""
        try {
            subject = mapping[input]!!
        } catch (e: Exception) {
            println("Studium nicht gefunden")
            continue
        }
        println("Läd gerade deine nächsten LV's")
        var subjects = getAllLV(subject)

        val yourPlan = subjects.toList().sortedBy { (key, value) -> value }.toMap()

        var counter = 0

        if (yourPlan.size == 0) println("Konnten keine Termine für Dich finden")

        for (it in yourPlan) {
            if (it.value.isAfter(LocalDateTime.now())) {
                counter++
                if (counter > 10) break
                println("Fach: " + it.key.substring(0, it.key.length - 1) + " ist um " + "${it.value}")
            }
        }
        println("Drücke eine Taste um weiterzugehen")
        subjects.clear()
        readLine()
    }

}