package io.kazak.model

public interface Event {

    fun type(): EventType

    fun timeSlot(): TimeSlot

}
