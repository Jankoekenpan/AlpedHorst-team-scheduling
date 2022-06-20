package nl.alpedhorst.teamscheduling

import java.io.{BufferedReader, File, FileInputStream, FileOutputStream, InputStreamReader, PrintWriter}
import java.net.http.HttpRequest.{BodyPublisher, BodyPublishers}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.{Duration, LocalDateTime}

import com.github.tototoshi.csv.{CSVReader, defaultCSVFormat}

object IO {

    def readEndPoint(textFile: File): String = {
        var endPoint = System.getenv("ENDPOINT")
        if (endPoint == null || endPoint.isBlank) {
            val reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)))
            endPoint = reader.readLine()
        }
        endPoint
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

    def readCSV(inputFile: File): List[Map[String, String]] = {
        val reader = CSVReader.open(inputFile)(using defaultCSVFormat)
        reader.allWithHeaders()
    }

    def writeFile(outputFile: File, schedule: Schedule, eventStart: LocalDateTime, slotDuration: Duration, invalidTeams: Iterable[String]): Unit = {
        if (outputFile.exists()) outputFile.delete()
        outputFile.createNewFile()

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
        val writer = new PrintWriter(new FileOutputStream(outputFile))

        writer.println("=== Schedule ===")
        for ((team, index) <- schedule.zipWithIndex) {
            val timeSlot = convertIndexToTimeSlot(index, eventStart, slotDuration)
            val teamName = if team == null then "---" else team.name
            writer.println(s"${formatter.format(timeSlot.start)} - ${formatter.format(timeSlot.end)}: ${teamName}")
        }
        if (schedule.conflictingTeams.nonEmpty) {
            writer.println(System.lineSeparator())
            writer.println("=== Conflicting Teams ===")
            for (team <- schedule.conflictingTeams) {
                writer.println(team.name)
            }
        }
        if (invalidTeams.nonEmpty) {
            writer.println(System.lineSeparator())
            writer.println("=== Invalid Teams ===")
            for (invalidTeam <- invalidTeams) {
                writer.println(invalidTeam)
            }
        }

        writer.flush()
        writer.close()
    }

}
