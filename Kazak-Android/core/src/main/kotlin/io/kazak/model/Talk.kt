package io.kazak.model

public data class Talk(val id : String, val name : String, val timeSlot: TimeSlot, val rooms: List<Room>, val speakers: Speakers) : Session {

    override fun type(): EventType {
        return EventType.TALK
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

    fun speakersNames(): String {
        return speakers.names()
    }

    fun track(): Track {
        return Track("1234", "LOL", 0xFF2C69CD.toInt())
    }

}
