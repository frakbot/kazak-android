package io.kazak.model

public data class Track(val id: String, val name: String, val color: Int) {

    fun id(): String {
        return id
    }

    fun name(): String {
        return name
    }

    fun color(): Int {
        return color
    }

}
