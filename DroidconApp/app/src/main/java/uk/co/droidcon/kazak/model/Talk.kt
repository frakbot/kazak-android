package uk.co.droidcon.kazak.model

import java.util.*

public data class Talk(val id : String, val name : String, val timeSlot: TimeSlot, val room: Room, val speakers: Speakers) {

    fun getSpeakersNames(): String = speakers.getNames()

}
