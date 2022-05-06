package nl.alpedhorst.teamscheduling

import nl.alpedhorst.teamscheduling.*

import java.io.File
import scala.annotation.targetName

extension (lhs: Boolean)
    @targetName("implies")
    def ==>(rhs: => Boolean): Boolean = !lhs || rhs

@main def main(): Unit = {

    val endpoint = IO.readEndPoint(new File("endpoint"))
    val json = IO.fetchJson(endpoint)
    //println(json)

    json match {
        case ujson.Arr(jsonValues) =>
            val jsonTeams = jsonValues
                .filter(_ match { case ujson.Obj(map) if map.contains("Teamnaam") => true; case _ => false})
                .map(_.asInstanceOf[ujson.Obj])
                .map(JsonTeam.jsonTeam)
            val teams = jsonTeams.map(jsonTeam => TimeSlot.convertTeam(jsonTeam, eventStartTime, slotDuration))
            //println(teams)
            val schedules = Schedule.calculate(teams.toList, slotCount)
            val schedule = schedules.head
            println(schedule)
            assert(schedule.zipWithIndex.forall((team, slot) => (team != null) ==> team.canMakeIt(slot)), "Not all teams can make it.")
            IO.writeFile(new File("schedule.txt"), schedule, eventStartTime, slotDuration)
        case _ =>
            throw new RuntimeException("Expected json array")
    }

}