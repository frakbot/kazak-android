package io.kazak.model

public interface Event {

    fun id(): Id

    fun type(): EventType

    fun name(): String

    fun timeSlot(): TimeSlot

    fun description(): String?

}
