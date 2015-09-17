package io.kazak.model

public data class Ceremony(val id: Id, val name: String, val timeSlot: TimeSlot, val rooms: List<Room>) : Session {

    override fun id(): Id {
        return id
    }

    override fun type(): EventType {
        return EventType.CEREMONY
    }

    override fun name(): String {
        return name
    }

    override fun rooms(): List<Room> {
        return rooms
    }

    override fun timeSlot(): TimeSlot {
        return timeSlot
    }

}
