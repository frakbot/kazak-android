package io.kazak.api.json

import io.kazak.api.json.model.JsonEvent
import io.kazak.api.json.model.JsonPresenter
import io.kazak.api.json.model.JsonRoom
import io.kazak.model.*
import io.kazak.utils.SimpleDateFormatThreadSafe
import java.text.ParseException
import java.util.Calendar
import java.util.Date

private val ISO_DATE_FORMATTER = SimpleDateFormatThreadSafe("yyyy-MM-dd'T'HH:mm:ss")

fun asSchedule(jsonTalks: List<JsonEvent>) = asScheduleInternal(jsonTalks, ::asEvent)

fun asEvent(jsonEvent: JsonEvent) = asEventInternal(jsonEvent, ::asTimeSlot, ::asRoom, ::asSpearkers)

fun asTimeSlot(start: String, end: String) = asTimeSlotInternal(start, end, ::asDate)

fun asDate(isoDate: String): Date? {
    return try {
        ISO_DATE_FORMATTER.parse(isoDate)
    } catch (e: ParseException) {
        null
    }
}

fun asRoom(jsonRoom: JsonRoom): Room {
    return Room(jsonRoom.id, jsonRoom.name)
}

fun asSpearkers(jsonPresenters: List<JsonPresenter>): Speakers {
    return asSpearkersInternal(jsonPresenters, ::asSpearker)
}

fun asSpearker(jsonPresenter: JsonPresenter): Speaker {
    return Speaker(
            jsonPresenter.id,
            jsonPresenter.name,
            jsonPresenter.company,
            jsonPresenter.bio,
            jsonPresenter.pic,
            jsonPresenter.social
    )
}

private fun asScheduleInternal(jsonTalks: List<JsonEvent>, asEvent: (JsonEvent) -> Event?): Schedule {
    val calendar = Calendar.getInstance();
    val talks = jsonTalks.map {
        asEvent(it)
    }.requireNoNulls().groupBy {
        calendar.setTime(it.timeSlot().start)
        calendar.get(Calendar.DAY_OF_YEAR)
    }.map {
        val date = it.getValue().get(0).timeSlot().start // TODO: improve this
        Day(date, it.getValue())
    }
    return Schedule(talks)
}

fun asEventInternal(
        jsonEvent: JsonEvent,
        asTimeSlot: (String, String) -> TimeSlot?,
        asRoom: (JsonRoom) -> Room,
        asSpeakers: (List<JsonPresenter>) -> Speakers
): Event? {
    val timeSlot = asTimeSlot(jsonEvent.startDate, jsonEvent.endDate) ?: return null

    val jsonRoom = jsonEvent.room ?: return null
    val jsonPresenters = jsonEvent.presenters ?: return null
    return Talk(jsonEvent.id, jsonEvent.name, timeSlot, asRoom(jsonRoom), asSpeakers(jsonPresenters))
}

private fun asTimeSlotInternal(start: String, end: String, asDate: (String) -> Date?): TimeSlot? {
    val startDate = asDate(start)
    val endDate = asDate(end)
    if (startDate == null || endDate == null) {
        return null;
    }
    return TimeSlot(startDate, endDate)
}

fun asSpearkersInternal(jsonPresenters: List<JsonPresenter>, asSpeaker: (JsonPresenter) -> Speaker): Speakers {
    return Speakers(jsonPresenters.map { asSpeaker(it) })
}
