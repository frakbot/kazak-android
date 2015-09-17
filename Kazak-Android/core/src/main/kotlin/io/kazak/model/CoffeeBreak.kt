package io.kazak.model

public data class CoffeeBreak(val id: Id, val name: String, val description: String?, val timeSlot: TimeSlot) : Event {

    override fun id(): Id {
        return id
    }

    override fun type(): EventType {
        return EventType.COFFEE_BREAK
    }

    override fun name(): String {
        return name
    }

    override fun description(): String? {
        return description
    }

    override fun timeSlot(): TimeSlot {
        return timeSlot
    }

}
