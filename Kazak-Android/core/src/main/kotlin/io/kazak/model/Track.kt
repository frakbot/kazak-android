package io.kazak.model

data class Track(val id: Id, val name: String, val color: Color?) {

    fun id(): Id {
        return id
    }

    fun name(): String {
        return name
    }

    fun color(): Color? {
        return color
    }

}
