package io.kazak.model

public data class Talk(val id : String, val name : String, val timeSlot: TimeSlot, val room: Room, val speakers: Speakers) {

    fun getSpeakersNames(): String {
        return speakers.getNames()
    }

}
