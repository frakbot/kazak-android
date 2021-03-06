package io.kazak.model

data class Speakers(val speakers: List<Speaker>) {

    fun names(): String {
        return speakers.joinToString(", ", transform = { speaker -> speaker.name })
    }

}
