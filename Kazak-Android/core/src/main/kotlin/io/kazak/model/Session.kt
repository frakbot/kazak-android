package io.kazak.model

interface Session : Event {

    fun rooms(): List<Room>

}
