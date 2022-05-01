val slotCount = 147

type Slot = Int
val minSlot = 0
val maxSlot = slotCount - 1

type Schedule = IndexedSeq[Team]
val emptySchedule: Schedule = IndexedSeq.fill(slotCount)(null)
extension (schedule: Schedule)
    def isAvailable(slot: Slot): Boolean = schedule(slot) == null
    def setTeam(slot: Slot, team: Team): Schedule = schedule.updated(slot, team)

class Team(private val name: String, available: Set[Slot], unavailable: Set[Slot]) {
     def canMakeIt(slot: Slot): Boolean = {
         var result = true
         if (available != null) result &= available.contains(slot)
         if (unavailable != null) result &= !unavailable.contains(slot)
         result
     }

    override def equals(obj: Any): Boolean = obj match {
        case that: Team => this.name == that.name
        case _ => false
    }

    override def hashCode(): Int = name.hashCode

    override def toString(): String = name
}

def calculate(teams: Set[Team]): List[Schedule] = {
    def successorSchedules(acc: Schedule, team: Team): List[Schedule] = {
        val result = List.newBuilder[Schedule]

        var slotCandidate = minSlot
        while (slotCandidate <= maxSlot) {
            if (acc.isAvailable(slotCandidate) && team.canMakeIt(slotCandidate)) {
                result.addOne(acc.setTeam(slotCandidate, team))
            }

            slotCandidate += 1
        }

        result.result()
    }

    var schedules: List[Schedule] = List(emptySchedule)
    for (team <- teams) {
        schedules = schedules.flatMap(schedule => successorSchedules(schedule, team))
    }

    schedules
}


@main def main(): Unit = {

    //TODO read file contents
    //TODO perform calculation
    //TODO pick a schedule (need to check whether a valid schedule can exist)
    //TODO write out file

}