package nl.alpedhorst.teamscheduling

import nl.alpedhorst.teamscheduling.*

import java.time.{Duration, LocalDateTime}

type Schedule = IndexedSeq[Team]
extension (schedule: Schedule)
    def isAvailable(slot: Slot): Boolean = schedule(slot) == null
    def setTeam(slot: Slot, team: Team): Schedule = schedule.updated(slot, team)

object Schedule {
    def emptySchedule(slotCount: Int): Schedule = IndexedSeq.fill(slotCount)(null)

    //TODO use LazyList[Schedule] instead?

    def calculate(teams: List[Team], slotCount: Int): List[Schedule] = {
        var schedules: List[Schedule] = List(emptySchedule(slotCount))
        for (team <- teams) {
            schedules = schedules.flatMap(schedule => successorSchedules(schedule, team))
        }
        schedules
    }

    private def successorSchedules(acc: Schedule, team: Team): List[Schedule] = {
        val result = List.newBuilder[Schedule]

        var slotCandidate = 0
        while (slotCandidate < acc.length) {
            if (acc.isAvailable(slotCandidate) && team.canMakeIt(slotCandidate)) {
                result.addOne(acc.setTeam(slotCandidate, team))
            }

            slotCandidate += 1
        }

        result.result()
    }

    def allTeamsCanMakeIt(schedule: Schedule): Boolean =
        schedule.zipWithIndex.forall((team, index) => team.canMakeIt(index))
    def allPositionsFilled(schedule: Schedule): Boolean =
        !schedule.exists(_ == null)
    def containsAllTeamsExactlyOnce(schedule: Schedule, teams: Iterable[Team]): Boolean =
        schedule.toSet.size == teams.size

}
