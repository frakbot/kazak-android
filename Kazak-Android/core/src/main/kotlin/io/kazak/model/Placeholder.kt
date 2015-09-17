package io.kazak.model

public data class Placeholder(val id: Id, val name: String, val description: String?, val timeSlot: TimeSlot, val rooms: List<Room>) : Session {

    override fun id(): Id {
        return id
    }

    override fun type(): EventType {
        return EventType.PLACEHOLDER
    }

    override fun name(): String {
        return name
    }

    override fun description(): String? {
        return description
    }

    override fun rooms(): List<Room> {
        return rooms
    }

    override fun timeSlot(): TimeSlot {
        return timeSlot
    }

}
