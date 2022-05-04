package nl.alpedhorst.teamscheduling

import nl.alpedhorst.teamscheduling.*

type Schedule = IndexedSeq[Team]
val emptySchedule: Schedule = IndexedSeq.fill(slotCount)(null)  //TODO parameterize slotCount
extension (schedule: Schedule)
    def isAvailable(slot: Slot): Boolean = schedule(slot) == null
    def setTeam(slot: Slot, team: Team): Schedule = schedule.updated(slot, team)

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

def calculate(teams: List[Team]): List[Schedule] = {
    var schedules: List[Schedule] = List(emptySchedule)
    for (team <- teams) {
        schedules = schedules.flatMap(schedule => successorSchedules(schedule, team))
    }
    schedules
}
