package io.kazak.model

public data class Talk(
        val id : Id,
        val name : String,
        val description : String?,
        val timeSlot: TimeSlot,
        val rooms: List<Room>,
        val speakers: Speakers,
        val track: Track?
) : Session {

    override fun id(): Id {
        return id
    }

    override fun type(): EventType {
        return EventType.TALK
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

    fun speakersNames(): String {
        return speakers.names()
    }

    fun track(): Track {
        val colors = arrayOf(0xFF2C69CD, 0xFFE91E63, 0xFF009688, 0xFFEF6C00)
        val colorIndex = (Math.random() * colors.size).toInt()
        val color = Color(colors[colorIndex].toInt())
        return Track(Id("1234"), "LOL", color)
    }

}
