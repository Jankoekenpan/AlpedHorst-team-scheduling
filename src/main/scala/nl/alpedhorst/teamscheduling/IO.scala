package nl.alpedhorst.teamscheduling

import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}
import java.net.http.HttpRequest.{BodyPublisher, BodyPublishers}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

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

}
