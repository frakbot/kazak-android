package io.kazak.model

public data class Talk(val id : String, val name : String, val timeSlot: TimeSlot, val room: Room, val speakers: Speakers) : Event {

    override fun type(): EventType {
        return EventType.TALK
    }

    fun speakersNames(): String {
        return speakers.names()
    }

    override fun timeSlot(): TimeSlot {
        return timeSlot
    }

}
