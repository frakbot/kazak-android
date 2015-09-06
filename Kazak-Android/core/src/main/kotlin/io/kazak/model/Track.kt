package io.kazak.model

public data class Track(val id: String, val name: String, val color: Color?) {

    fun id(): String {
        return id
    }

    fun name(): String {
        return name
    }

    fun color(): Color? {
        return color
    }

}
