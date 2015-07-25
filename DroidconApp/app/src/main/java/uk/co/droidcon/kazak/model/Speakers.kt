package uk.co.droidcon.kazak.model

public data class Speakers(val speakers: List<Speaker>) {

    fun getNames() : String = speakers.joinToString(", ", transform = {speaker -> speaker.name})

}

