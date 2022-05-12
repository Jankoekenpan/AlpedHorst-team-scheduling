package nl.alpedhorst.teamscheduling

import org.scalatest.*
import flatspec.*
import matchers.*

import java.time.{Duration, LocalDateTime}

//  1. The schedule contains all teams, and all teams occur exactly once
//  2. The teams can make it in their scheduled time slot
//  3. The schedule has no empty slots

object ScheduleTest {

    val exampleTeam1 = Team("1", _ % 2 == 0)
    val exampleTeam2 = Team("2", _ == 2)
    val exampleTeam3 = Team("3", _ == 4)
    val exampleTeam4 = Team("4", _ => false)
    val exampleTeams = List(exampleTeam1, exampleTeam2, exampleTeam3, exampleTeam4)

}

class ScheduleSpec extends AnyFlatSpec with should.Matchers {
    import ScheduleTest._

    "A schedule" should "contain all teams exactly once" in {
        val schedules = Schedule.calculate(exampleTeams, exampleTeams.length)
        assert(schedules.forall(_.containsAllTeamsExactlyOnce(exampleTeams)))
    }

    "All teams" should "be able to make it in their scheduled slot" in {
        val schedules = Schedule.calculate(exampleTeams, exampleTeams.length)
        assert(schedules.forall(_.allTeamsCanMakeIt))
    }

    "A schedule" should "not contain empty slots" in {
        val schedules = Schedule.calculate(exampleTeams, exampleTeams.length)
        assert(schedules.forall(_.allPositionsFilled))
    }

}