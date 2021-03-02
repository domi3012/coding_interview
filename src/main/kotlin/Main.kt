import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException



fun main() {
    var mapping = mutableMapOf<String, String>()
    var document: Document? = null
    try {
        val addExamResponse = Jsoup.connect("https://ufind.univie.ac.at/de/vvz.html")
            .method(Connection.Method.GET).timeout(10 * 1000)
            .userAgent("Opera")
            .execute()
        document = addExamResponse.parse()
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
        }

        val subject = mapping[input]!!
        val thread = LVs(subject)
        thread.start()
        println("Checking what you have to see next...")
        thread.join()
        var subjects = thread.subjects


        val yourPlan = subjects.toList().sortedBy { (key, value) -> value }.toMap()

        var counter = 0

        for (it in yourPlan) {
            counter++
            if (counter > 10) break
            println("Fach: " + it.key.substring(0, it.key.length - 1) + " ist um " + "${it.value}")
        }
        println("Drücke eine Taste um weiterzugehen")
        subjects.clear()
        readLine()
    }

}