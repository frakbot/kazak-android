package io.kazak.model

import io.kazak.base.DeveloperError
import java.util.Date

public data class DateBound(val date: Date) : ScheduleBound {

    constructor(date: Long) : this(Date(date))

    override fun compareTo(other: ScheduleBound): Int {
        if (other is DateBound) {
            return date.compareTo(other.date)
        }
        throw DeveloperError("DateBound can only be compared with other DateBounds")
    }

    public fun getTime(): Long {
        return date.getTime()
    }

}
