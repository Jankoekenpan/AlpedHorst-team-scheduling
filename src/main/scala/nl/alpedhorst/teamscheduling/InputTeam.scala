package nl.alpedhorst.teamscheduling

import java.time.LocalTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}

object InputTeam {

    def jsonTeam(jsonTeam: ujson.Obj): InputTeam = {
        InputTeam(teamName(jsonTeam), unavailability(jsonTeam))
    }

    def csvTeam(csvTeam: Map[String, String]): InputTeam = {
        InputTeam(teamName(csvTeam), unavailability(csvTeam))
    }

    def teamName(teamJson: ujson.Obj): String =
        teamJson("Teamnaam").str

    def teamName(csvTeam: Map[String, String]): String =
        csvTeam("Teamnaam")

    private def convertTime(time: String): LocalTime = {
        var localTime: LocalTime = try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
        } catch {
            case e: DateTimeParseException =>
               LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm"))
        }
        localTime
    }

    private def fixUpEndTime(endTime: LocalTime): LocalTime =
        if LocalTime.MIN.equals(endTime) then LocalTime.MAX else endTime

    def unavailability(teamJson: ujson.Obj): Unavailability = {
        val mapBuilder = Map.newBuilder[Day, Duration]
        for (day <- 1 to 7) {
            val wholeDay: ujson.Value = teamJson(s"heledag_$day")
            val durationForCurrentDay: Duration = wholeDay match {
                case ujson.Str(strValue) if "1" == strValue => Duration.WholeDay
                case ujson.Bool(bool) if bool => Duration.WholeDay
                case _ =>
                    val time1 = teamJson(s"tijd_${day}1").str
                    val time2 = teamJson(s"tijd_${day}2").str
                    val time3 = teamJson(s"tijd_${day}3").str
                    val time4 = teamJson(s"tijd_${day}4").str

                    assert(time1.isBlank == time2.isBlank, s"invalid interval: time1=\"$time1\", time2=\"$time2\"")
                    assert(time3.isBlank == time3.isBlank, s"invalid interval: time3=\"$time3\", time4=\"$time4\"")

                    val seqBuilder = Seq.newBuilder[Interval]

                    if (!time1.isBlank)
                        seqBuilder.addOne(Interval(convertTime(time1), fixUpEndTime(convertTime(time2))))
                    if (!time3.isBlank)
                        seqBuilder.addOne(Interval(convertTime(time3), fixUpEndTime(convertTime(time4))))

                    Duration.Intervals(seqBuilder.result())
            }
            mapBuilder.addOne(day.asInstanceOf[Day], durationForCurrentDay)
        }
        mapBuilder.result()
    }

    def unavailability(teamCSV: Map[String, String]): Unavailability = {
        val mapBuilder = Map.newBuilder[Day, Duration]
        for (day <- 1 to 7) {
            val wholeDay = teamCSV(s"heledag_$day")
            val durationForCurrentDay: Duration = wholeDay match {
                case "1" => Duration.WholeDay
                case _ =>
                    val time1 = teamCSV(s"tijd_${day}1")
                    val time2 = teamCSV(s"tijd_${day}2")
                    val time3 = teamCSV(s"tijd_${day}3")
                    val time4 = teamCSV(s"tijd_${day}4")

                    assert(time1.isBlank == time2.isBlank, s"invalid interval: time1=\"$time1\", time2=\"$time2\"")
                    assert(time3.isBlank == time3.isBlank, s"invalid interval: time3=\"$time3\", time4=\"$time4\"")

                    val seqBuilder = Seq.newBuilder[Interval]

                    if (!time1.isBlank)
                        seqBuilder.addOne(Interval(convertTime(time1), fixUpEndTime(convertTime(time2))))
                    if (!time3.isBlank)
                        seqBuilder.addOne(Interval(convertTime(time3), fixUpEndTime(convertTime(time4))))

                    Duration.Intervals(seqBuilder.result())
            }
            mapBuilder.addOne(day.asInstanceOf[Day], durationForCurrentDay)
        }
        mapBuilder.result()
    }

}

class InputTeam(val name: String, val unavailability: Unavailability) {

    override def equals(o: Any): Boolean = o match {
        case that: InputTeam => this.name == that.name
        case _ => false
    }

    override def hashCode: Int = java.util.Objects.hashCode(name)

    override def toString: String = name
}

type Day = 1 | 2 | 3 | 4 | 5 | 6 | 7
type Unavailability = Map[Day, Duration]

enum Duration {
    case WholeDay
    case Intervals(intervals: Seq[Interval])
}

case class Interval(from: LocalTime, to: LocalTime) {
    assert(from.isBefore(to), s"\"from\" must be earlier in time then \"to\". Got from=${from}, to=${to}.")

    def contains(localTime: LocalTime): Boolean = from.isBefore(localTime) && localTime.isBefore(to)

}
