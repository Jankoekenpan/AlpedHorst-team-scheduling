package nl.alpedhorst.teamscheduling

import nl.alpedhorst.teamscheduling.*

import java.time.{Duration, LocalDateTime}

type Schedule = IndexedSeq[Team]
extension (schedule: Schedule)
    def isAvailable(slot: Slot): Boolean = schedule(slot) == null
    def setTeam(slot: Slot, team: Team): Schedule = schedule.updated(slot, team)

object Schedule {
    def emptySchedule(slotCount: Int): Schedule = IndexedSeq.fill(slotCount)(null)

    def calculate(teams: List[Team], slotCount: Int): LazyList[Schedule] = {
        var schedules: LazyList[Schedule] = LazyList(emptySchedule(slotCount))
        for (team <- teams) {
            schedules = schedules.flatMap(schedule => successorSchedules(schedule, team))
        }
        schedules
    }

    private def successorSchedules(acc: Schedule, team: Team): LazyList[Schedule] = {
        val result = LazyList.newBuilder[Schedule]

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
        !schedule.contains(null)
    def containsAllTeamsExactlyOnce(schedule: Schedule, teams: Iterable[Team]): Boolean =
        schedule.toSet.size == teams.size

}
