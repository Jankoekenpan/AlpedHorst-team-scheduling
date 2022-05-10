package nl.alpedhorst.teamscheduling

import java.time.temporal.ChronoUnit
import java.time.{Duration, LocalDateTime}

//estimated values:
val eventStartTime  = LocalDateTime.of(2022, 8, 1, 12, 0)   // 1st of August in year 2022 at 12:00
val eventEndTime    = LocalDateTime.of(2022, 8, 7, 15, 0)   // 7th of August in year 2022 at 15:00
val slotDuration    = Duration.ofHours(1)                                                        // a time slot takes one hour
val slotCount       = calculateSlotCount(eventStartTime, eventEndTime, slotDuration)        // 147 slots

type Slot = Int
extension (localTime: java.time.LocalTime)
    def <(other: java.time.LocalTime): Boolean = localTime.isBefore(other)
    def >(other: java.time.LocalTime): Boolean = localTime.isAfter(other)
    def <=(other: java.time.LocalTime): Boolean = localTime.compareTo(other) <= 0
    def >=(other: java.time.LocalTime): Boolean = localTime.compareTo(other) >= 0

object TimeSlot {
    import nl.alpedhorst.teamscheduling.Duration.{WholeDay, Intervals}
    import nl.alpedhorst.teamscheduling.Interval

    def convertTeam(jsonTeam: InputTeam, eventStart: LocalDateTime, slotDuration: Duration): Team = {
        val teamName: String = jsonTeam.name
        val unavailable: Slot => Boolean = convertUnavailable(jsonTeam.unavailability, eventStart, slotDuration)
        Team(teamName, unavailable)
    }

    def convertUnavailable(unavailability: Unavailability, eventStart: LocalDateTime, slotDuration: Duration)(slot: Slot): Boolean = {
        val timeSlot: TimeSlot = convertIndexToTimeSlot(slot, eventStart, slotDuration)

        val day1: Day = timeSlot.start.getDayOfMonth.asInstanceOf[Day]
        val day2: Day = timeSlot.end.getDayOfMonth.asInstanceOf[Day]

        (unavailability(day1), unavailability(day2)) match {
            case (WholeDay, _) => true
            case (_, WholeDay) => true
            case (Intervals(intervals1), Intervals(intervals2)) =>
                if (day1 == day2) {
                    //intervals1 == intervals2
                    intervals1.exists(interval => clashes(timeSlot, interval))
                } else {
                    intervals1.exists(interval => clashes(timeSlot, interval)) || intervals2.exists(interval => clashes(timeSlot, interval))
                }
        }
    }

    private def clashes(timeSlot: TimeSlot, interval: Interval): Boolean = {
        val slotStart = timeSlot.start.toLocalTime
        val slotEnd = timeSlot.end.toLocalTime

        interval.contains(slotStart)
            || interval.contains(slotEnd)
            || interval.from <= slotStart && interval.to >= slotEnd
            || slotStart <= interval.from && slotEnd >= interval.to
    }

}

def calculateSlotCount(start: LocalDateTime, end: LocalDateTime, slotDuration: Duration): Int =
    (start.until(end, ChronoUnit.MINUTES) / slotDuration.toMinutes).toInt

//alternative method of slot divisions, in case we can't reach 147 teams. in this case slotCount == teamCount
def calculateStart(end: LocalDateTime, slotCount: Int, slotDuration: Duration): LocalDateTime =
    end.minus(slotDuration.multipliedBy(slotCount))

def convertIndexToTimeSlot(index: Slot, eventStart: LocalDateTime, slotDuration: Duration): TimeSlot = {
    val startTime = eventStartTime.plus(slotDuration.multipliedBy(index))
    val endTime = startTime.plus(slotDuration)
    TimeSlot(startTime, endTime)
}

def convertTimeSlotToIndex(timeSlot: TimeSlot, eventStart: LocalDateTime, slotDuration: Duration): Slot = {
    assert(timeSlot.duration == slotDuration, "timeSlot has duration unequal to the schedule's slot duration")
    assert(!timeSlot.start.isBefore(eventStart), "timeSlot starts before the start of the event")

    val minutesAfterStart: Long = eventStart.until(timeSlot.start, ChronoUnit.MINUTES)
    (minutesAfterStart / slotDuration.toMinutes).toInt
}

class TimeSlot(val start: LocalDateTime, val end: LocalDateTime) {
    assert(start.isBefore(end), "start must be before end")

    def duration: Duration = Duration.ofMinutes(start.until(end, ChronoUnit.MINUTES))

    override def equals(o: Any): Boolean = o match {
        case that: TimeSlot => this.start == that.start && this.end == that.end
        case _ => false
    }

    override def hashCode: Int = java.util.Objects.hash(start, end)

    override def toString: String = s"TimeSlot(start=${start},end=${end})"
}

