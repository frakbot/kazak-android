package io.kazak.model

interface Event {

    fun id(): Id

    fun type(): EventType

    fun name(): String

    fun timeSlot(): TimeSlot

    fun description(): String?

}
