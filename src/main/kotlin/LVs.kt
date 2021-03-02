import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.lang.Exception
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class LVs(val url: String) : Thread() {
    val uni = "https://ufind.univie.ac.at/de/"
    val year = Calendar.getInstance().get(Calendar.YEAR)

    var subjects = mutableMapOf<String, LocalDateTime>()

    override fun run() {
        getAllLV(url)

    }



    fun getTimeLines(mapping: Map<String, String>) {
        mapping.forEach { key, value ->
            var document: Document?
            try {
                val response = Jsoup.connect("$uni$key")
                    .method(Connection.Method.GET)
                    .userAgent("Opera").timeout(10*1000)
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
            val mappingLV = document!!.select(".events .event")
            val date = mappingLV.select(".date")
            val hour = mappingLV.select(".time")
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
    }

    fun getAllLV(url: String) {
        var document: Document? = null
        var mappingValues = mutableMapOf<String, String>()
        try {
            val addExamResponse = Jsoup.connect("$uni$url")
                .method(Connection.Method.GET).timeout(10*1000)
                .userAgent("Opera")
                .execute()
            document = addExamResponse.parse()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var mappingLV = document!!.select(".what")
        mappingLV.forEach { mappingValues.put(it.attr("href"), it.text()) }
        mappingValues.remove(mappingValues.keys.last())
        getTimeLines(mappingValues)

    }
}

