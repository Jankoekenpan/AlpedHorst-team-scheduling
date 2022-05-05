package nl.alpedhorst.teamscheduling

import java.io.{BufferedReader, File, FileInputStream, FileOutputStream, InputStreamReader, PrintWriter}
import java.net.http.HttpRequest.{BodyPublisher, BodyPublishers}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.{Duration, LocalDateTime}

object IO {

    def readEndPoint(textFile: File): String = {
        val reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)))
        reader.readLine()
    }

    def fetchJson(endpoint: String): ujson.Value = {
        val httpClient = HttpClient.newHttpClient()
        val response = httpClient.send(HttpRequest.newBuilder(java.net.URI.create(endpoint)).build(), HttpResponse.BodyHandlers.ofInputStream())
        val inputStream = response.body()

        ujson.read(new ujson.Readable {
            override def transform[T](f: upickle.core.Visitor[_, T]): T = {
                try ujson.InputStreamParser.transform(inputStream, f)
                finally inputStream.close()
            }
        })
    }

    def writeFile(outputFile: File, schedule: Schedule, eventStart: LocalDateTime, slotDuration: Duration): Unit = {
        if (outputFile.exists()) outputFile.delete()
        outputFile.createNewFile()

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
        val writer = new PrintWriter(new FileOutputStream(outputFile))

        for ((team, index) <- schedule.zipWithIndex) {
            val timeSlot = convertIndexToTimeSlot(index, eventStart, slotDuration)
            val teamName = if team == null then "---" else team.name
            writer.println(s"${formatter.format(timeSlot.start)} - ${formatter.format(timeSlot.end)}: ${teamName}")
        }

        writer.flush()
        writer.close()
    }

}
