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
        val colors = arrayOf(0xFF2C69CD, 0xFFE91E63, 0xFF009688, 0xFFEF6C00)
        val colorIndex = (Math.random() * colors.size()).toInt()
        val color = colors[colorIndex].toInt()
        return Track("1234", "LOL", color)
    }

}
