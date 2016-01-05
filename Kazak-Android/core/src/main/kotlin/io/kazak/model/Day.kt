package io.kazak.model

import java.util.Date

public data class Day(val day: Date, val events: List<Event>) {

    fun getTalks(): List<Talk> {
        return events.filter { it.type() == EventType.TALK }.map { it as Talk }
    }

}
