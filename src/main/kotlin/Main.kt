import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.lang.Exception
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

val uni = "https://ufind.univie.ac.at/de/"
val year = Calendar.getInstance().get(Calendar.YEAR)

fun getTimeLineFormat(){
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy H:m")
    //var time = pair.second.text().split('-')
    //val begin = LocalTime.parse(time[0].trimEnd())
    //val dateTime = LocalDateTime.parse(pair.first.text()+"$year "+ "$begin", formatter)
}

fun getTimeLines(mapping: Map<String, String>): MutableMap<String, LocalDateTime> {
    var subjects = mutableMapOf<String, LocalDateTime>()
    mapping.forEach { key, value ->
        var document: Document?
        try {
            val response = Jsoup.connect("$uni$key")
                .method(Connection.Method.GET)
                .userAgent("Opera").timeout(10*100000)
                .execute()
            val statusCode = response.statusCode()
            if(statusCode == 200) {
                document = response.parse()
            }else{
                throw Exception("error with connection")
            }

        } catch (e: IOException) {
            e.printStackTrace()
            return@forEach
        }
        //TODO check when null
        val mappingLV = document!!.select(".usse-id-group")
        //println(mappingLV)
        //TODO extract dates from string maybe regex?
        val tmp = mappingLV.select(".eventinfo")
        val eventList = mappingLV.select(".events")
        val date = eventList.select(".date")
        val hour = eventList.select(".time")
        //println(tmp);
        var counter = 0
        date.zip(hour).forEach { pair ->
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy H:m")
            var time = pair.second.text().split('-')
            val begin = LocalTime.parse(time[0].trimEnd())
            val dateTime = LocalDateTime.parse(pair.first.text()+"$year "+ "$begin", formatter)

            subjects.put("$value$counter", dateTime )
            counter++

        }
    }
    return subjects
}

fun getAllLV(url: String): MutableMap<String, LocalDateTime> {
    var document: Document? = null
    var mappingValues = mutableMapOf<String, String>()
    try {
        val addExamResponse = Jsoup.connect("$uni$url")
            .method(Connection.Method.GET).timeout(10*100000)
            .userAgent("Opera")
            .execute()
        document = addExamResponse.parse()
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
        val addExamResponse = Jsoup.connect("https://ufind.univie.ac.at/de/vvz.html")
            .method(Connection.Method.GET).timeout(10 * 100000)
            .userAgent("Opera")
            .execute()
        document = addExamResponse.parse()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    val mappingHref = document!!.select(".link")
    mappingHref.forEach { mapping.put(it.text(), it.attr("href"))}
    mapping.remove(mapping.keys.first())



        println("Zur Zeit gibt es diese Studienrichtungen")
        mapping.forEach { it -> println(it.key) }
        println("Wähle dein Studium aus")
        var input: String? = ""
        while (input == "") {
            input = readLine()
        }

        val subject = mapping[input]!!
        //val thread = LVs(subject)
        //thread.start()
        //println("Checking what you have to see next...")
        //thread.join()
        var subjects = getAllLV(subject)


        val yourPlan = subjects.toList().sortedBy { (key, value) -> value }.toMap()

        var counter = 0

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