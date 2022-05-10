package nl.alpedhorst.teamscheduling

import org.scalatest.*
import flatspec.*
import matchers.*

import java.time.LocalTime

object AvailabilityTest {
    val teamJson =
        """{
          |  "Teamnaam": "Some hypothetical team",
          |  "tijd_11": "00:01",
          |  "tijd_12": "15:30",
          |  "tijd_13": "17:00",
          |  "tijd_14": "00:00",
          |  "tijd_21": "",
          |  "tijd_22": "",
          |  "tijd_23": "",
          |  "tijd_24": "",
          |  "heledag_2": "1",
          |  "tijd_31": "",
          |  "tijd_32": "",
          |  "tijd_33": "",
          |  "tijd_34": "",
          |  "heledag_3": "1",
          |  "tijd_41": "",
          |  "tijd_42": "",
          |  "tijd_43": "",
          |  "tijd_44": "",
          |  "heledag_4": "1",
          |  "tijd_51": "",
          |  "tijd_52": "",
          |  "tijd_53": "",
          |  "tijd_54": "",
          |  "heledag_5": "1",
          |  "tijd_61": "",
          |  "tijd_62": "",
          |  "tijd_63": "",
          |  "tijd_64": "",
          |  "heledag_6": "1",
          |  "tijd_71": "",
          |  "tijd_72": "",
          |  "tijd_73": "",
          |  "tijd_74": "",
          |  "heledag_7": "1",
          |  "heledag_1": ""
          |}
          |""".stripMargin

    val json = ujson.read(ujson.Readable.fromString(teamJson)).asInstanceOf[ujson.Obj]

}

class AvailabilitySpec extends AnyFlatSpec with should.Matchers {
    import AvailabilityTest.*

    "time" should "convert correctly" in {
        val jsonTeam = InputTeam.jsonTeam(json)

        val expected: Unavailability = Map(
            1 -> Duration.Intervals(Seq(
                Interval(LocalTime.of(00, 01), LocalTime.of(15, 30)),
                Interval(LocalTime.of(17, 00), LocalTime.MAX))),
            2 -> Duration.WholeDay,
            3 -> Duration.WholeDay,
            4 -> Duration.WholeDay,
            5 -> Duration.WholeDay,
            6 -> Duration.WholeDay,
            7 -> Duration.WholeDay,
        )

        assert(jsonTeam.unavailability == expected)
    }

}