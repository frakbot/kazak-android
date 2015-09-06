package io.kazak.model

public data class Placeholder(val id: String, val name: String, val timeSlot: TimeSlot, val rooms: List<Room>) : Session {

    override fun type(): EventType {
        return EventType.PLACEHOLDER
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
