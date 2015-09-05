package io.kazak.model

public interface Event {

    fun type(): EventType

    fun name(): String

    fun timeSlot(): TimeSlot

}
