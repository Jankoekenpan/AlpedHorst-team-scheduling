package nl.alpedhorst.teamscheduling

import nl.alpedhorst.teamscheduling.Slot

class Team(val name: String, unavailable: Slot => Boolean) {
    def canMakeIt(slot: Slot): Boolean = !unavailable(slot)

    override def equals(obj: Any): Boolean = obj match {
        case that: Team => this.name == that.name
        case _ => false
    }

    override def hashCode(): Int = name.hashCode

    override def toString(): String = name
}