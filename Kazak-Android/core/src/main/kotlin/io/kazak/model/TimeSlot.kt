package io.kazak.model

import java.util.*

public data class TimeSlot(val start: DateBound, val end: DateBound) {

    constructor(start: Date, end: Date) : this(DateBound(start), DateBound(end))

}
