package nl.alpedhorst.teamscheduling

import nl.alpedhorst.teamscheduling.Slot

class Team(private val name: String, available: Set[Slot], unavailable: Set[Slot]) {
    def canMakeIt(slot: Slot): Boolean = {  //TODO TriState: Preferred | Available | Unavailable
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