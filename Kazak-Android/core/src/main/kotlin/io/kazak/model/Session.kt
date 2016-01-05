package io.kazak.model

public interface Session : Event {

    fun rooms(): List<Room>

}
