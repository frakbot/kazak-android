package io.kazak.model

public data class Speakers(val speakers: List<Speaker>) {

    fun getNames() : String {
        return speakers.joinToString(", ", transform = { speaker -> speaker.name })
    }

}
