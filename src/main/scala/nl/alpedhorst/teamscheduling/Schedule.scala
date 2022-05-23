package nl.alpedhorst.teamscheduling

import nl.alpedhorst.teamscheduling.*

import java.time.{Duration, LocalDateTime}

final case class Schedule(schedule: IndexedSeq[Team], conflictingTeams: Set[Team]) extends IndexedSeq[Team] {

    def isAvailable(slot: Slot): Boolean = schedule(slot) == null
    def setTeam(slot: Slot, team: Team): Schedule = Schedule(schedule.updated(slot, team), conflictingTeams)
    override def length: Int = schedule.length
    override def apply(index: Int): Team = schedule(index)

    override def toString: String = s"Schedule(schedule=${schedule}, conflicitingTeams=${conflictingTeams})"

    def allTeamsCanMakeIt: Boolean = schedule.zipWithIndex.forall((team, index) => team.canMakeIt(index))
    def allPositionsFilled: Boolean = !schedule.contains(null)
    def containsAllTeamsExactlyOnce(teams: Iterable[Team]): Boolean = schedule.toSet.size == teams.size
}

object Schedule {
    def emptySchedule(slotCount: Int): Schedule = Schedule(IndexedSeq.fill(slotCount)(null), Set())

    def calculate(teams: List[Team], slotCount: Int): LazyList[Schedule] = {
        var schedules: LazyList[Schedule] = LazyList(emptySchedule(slotCount))
        for (team <- teams) {
            val succeedingSchedules = schedules.flatMap(schedule => successorSchedules(schedule, team))
            if (succeedingSchedules.isEmpty) { //team is conflicting.
                schedules = schedules.map(schedule => schedule.copy(conflictingTeams = schedule.conflictingTeams + team))
            } else {
                schedules = succeedingSchedules
            }
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

}
