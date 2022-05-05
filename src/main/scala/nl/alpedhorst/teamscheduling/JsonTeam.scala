package nl.alpedhorst.teamscheduling

import java.time.LocalTime
import java.time.format.DateTimeFormatter

object JsonTeam {

    def jsonTeam(jsonTeam: ujson.Obj): JsonTeam = {
        JsonTeam(teamName(jsonTeam), unavailability(jsonTeam))
    }

    def teamName(teamJson: ujson.Obj): String =
        teamJson("Teamnaam").str

    private def convertTime(time: String): LocalTime = {
        LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
    }

    def unavailability(teamJson: ujson.Obj): Unavailability = {
        val mapBuilder = Map.newBuilder[Day, Duration]
        for (day <- 1 to 7) {
            val wholeDay: ujson.Value = teamJson(s"heledag_$day")
            val durationForCurrentDay: Duration = wholeDay match {
                case ujson.Str(strValue) if "1" == strValue => Duration.WholeDay
                case ujson.Bool(bool) => Duration.WholeDay
                case _ =>
                    val time1 = teamJson(s"tijd_${day}1").str
                    val time2 = teamJson(s"tijd_${day}2").str
                    val time3 = teamJson(s"tijd_${day}3").str
                    val time4 = teamJson(s"tijd_${day}4").str

                    assert(time1.isBlank == time2.isBlank, s"invalid interval: time1=\"$time1\", time2=\"$time2\"")
                    assert(time3.isBlank == time3.isBlank, s"invalid interval: time3=\"$time3\", time4=\"$time4\"")

                    val seqBuilder = Seq.newBuilder[Interval]

                    if (!time1.isBlank)
                        seqBuilder.addOne(Interval(convertTime(time1), convertTime(time2)))
                    if (!time3.isBlank)
                        seqBuilder.addOne(Interval(convertTime(time3), convertTime(time4)))

                    Duration.Intervals(seqBuilder.result())
            }
            mapBuilder.addOne(day.asInstanceOf[Day], durationForCurrentDay)
        }
        mapBuilder.result()
    }

}

class JsonTeam(val name: String, val unavailability: Unavailability) {

    override def equals(o: Any): Boolean = o match {
        case that: JsonTeam => this.name == that.name
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

    def contains(localTime: LocalTime): Boolean = from.compareTo(localTime) <= 0 && to.compareTo(localTime) > 0;

}
