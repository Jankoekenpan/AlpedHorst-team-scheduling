package nl.alpedhorst.teamscheduling

import org.scalatest.*
import flatspec.*
import matchers.*
import nl.alpedhorst.teamscheduling.{Slot, TimeSlot, convertIndexToTimeSlot, convertTimeSlotToIndex}

import java.time.temporal.ChronoUnit
import java.time.{Duration, LocalDateTime}

object TimeTest {
    import nl.alpedhorst.teamscheduling.{eventStartTime, eventEndTime, slotDuration, slotCount}

    val duration: Duration = slotDuration
    val start: LocalDateTime = eventStartTime
    val end: LocalDateTime = eventEndTime

    val firstSlot: TimeSlot = TimeSlot(start, start.plus(duration))
    val lastSlot: TimeSlot = TimeSlot(end.minus(duration), end)
}

class TimeSlotSpec extends AnyFlatSpec with should.Matchers {
    import TimeTest.*

    "An index conversion round-trip" should "result in the same value as the starting value" in {
        var index   = convertTimeSlotToIndex(firstSlot, start, duration)
        var res     = convertIndexToTimeSlot(index, start, duration)
        res.shouldEqual(firstSlot)

        index       = convertTimeSlotToIndex(lastSlot, start, duration)
        res         = convertIndexToTimeSlot(index, start, duration)
        res.shouldEqual(lastSlot)

        val middle      = start.plusHours(start.until(end, ChronoUnit.HOURS) / 2)
        val middleSlot  = TimeSlot(middle, middle.plus(duration))
        index           = convertTimeSlotToIndex(middleSlot, start, duration)
        res             = convertIndexToTimeSlot(index, start, duration)
        res.shouldEqual(middleSlot)
    }

    "A TimeSlot conversion round-trip" should "result in the same value as the starting value" in {
        var index   = 0
        var res     = convertTimeSlotToIndex(convertIndexToTimeSlot(index, start, duration), start, duration)
        res.shouldEqual(index)

        index       = 146
        res         = convertTimeSlotToIndex(convertIndexToTimeSlot(index, start, duration), start, duration)
        res.shouldEqual(index)

        index       = 147 / 2
        res         = convertTimeSlotToIndex(convertIndexToTimeSlot(index, start, duration), start, duration)
        res.shouldEqual(index)
    }

}
