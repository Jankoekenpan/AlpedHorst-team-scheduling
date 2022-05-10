package nl.alpedhorst.teamscheduling

import nl.alpedhorst.teamscheduling.*

import java.io.File
import scala.annotation.targetName

extension (lhs: Boolean)
    @targetName("implies")
    inline def ==>(inline rhs: Boolean): Boolean = !lhs || rhs

@main def main(): Unit = {

    //TODO make the following changes:
    //TODO  0. read the team availability data from csv instead of json
    //TODO a few optimizations:
    //TODO  1. sort the teams by available time (lowest durations go first)
    //TODO  2. don't iterate over all indexes, only iterate over those indexes in which the team can make it (should perform better when many teams are only available in a small window)

    val endpoint = IO.readEndPoint(new File("endpoint"))
    val json = IO.fetchJson(endpoint)
    //println(json)

    json match {
        case ujson.Arr(jsonValues) =>
            val jsonTeams = jsonValues
                .filter(_ match { case ujson.Obj(map) if map.contains("Teamnaam") => true; case _ => false; })
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