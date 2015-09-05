package io.kazak.api

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

fun asEvent(jsonEvent: JsonEvent) = asEventInternal(jsonEvent, ::asTalk, ::asCoffeeBreak, ::asCeremony, ::asPlaceholder)

fun asTalk(jsonEvent: JsonEvent) = asTalkInternal(jsonEvent, ::asTimeSlot, ::asRoom, ::asSpearkers)

fun asCoffeeBreak(jsonEvent: JsonEvent) = asCoffeeBreakInternal(jsonEvent, ::asTimeSlot)

fun asPlaceholder(jsonEvent: JsonEvent) = asPlaceholderInternal(jsonEvent, ::asTimeSlot)

fun asCeremony(jsonEvent: JsonEvent) = asCeremonyInternal(jsonEvent, ::asTimeSlot, ::asRoom)

fun asTimeSlot(start: String?, end: String?) = asTimeSlotInternal(start, end, ::asDate)

fun asDate(isoDate: String?): Date? {
    if (isoDate == null) {
        return null
    }
    return try {
        ISO_DATE_FORMATTER.parse(isoDate)
    } catch (e: ParseException) {
        null
    }
}

fun asRoom(jsonRoom: JsonRoom?): Room? {
    if (jsonRoom == null) {
        return null
    }
    val id = jsonRoom.id ?: return null
    val name = jsonRoom.name ?: return null
    return Room(id, name)
}

fun asSpearkers(jsonPresenters: List<JsonPresenter>): Speakers {
    return asSpearkersInternal(jsonPresenters, ::asSpearker)
}

fun asSpearker(jsonPresenter: JsonPresenter): Speaker? {
    val id = jsonPresenter.id ?: return null
    val name = jsonPresenter.name ?: return null
    return Speaker(
            id,
            name,
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
    }.filterNotNull().groupBy {
        calendar.setTime(it.timeSlot().start)
        calendar.get(Calendar.DAY_OF_YEAR)
    }.map {
        val date = it.getValue().get(0).timeSlot().start // TODO: improve this
        Day(date, it.getValue())
    }
    return Schedule(talks)
}

private fun asEventInternal(
        jsonEvent: JsonEvent,
        asTalk: (JsonEvent) -> Talk?,
        asCoffeeBreak: (JsonEvent) -> CoffeeBreak?,
        asCeremony: (JsonEvent) -> Ceremony?,
        asPlaceHolder: (JsonEvent) -> Placeholder?
): Event? {
    return when (jsonEvent.type) {
        "TALK" -> asTalk(jsonEvent)
        "CEREMONY" -> asCeremony(jsonEvent)
        "COFFEE_BREAK" -> asCoffeeBreak(jsonEvent)
        "PLACEHOLDER" -> asPlaceHolder(jsonEvent)
        else -> null
    }
}

private fun asTalkInternal(
        jsonEvent: JsonEvent,
        asTimeSlot: (String?, String?) -> TimeSlot?,
        asRoom: (JsonRoom?) -> Room?,
        asSpeakers: (List<JsonPresenter>) -> Speakers
): Talk? {
    val id = jsonEvent.id ?: return null
    val name = jsonEvent.name ?: return null
    val timeSlot = asTimeSlot(jsonEvent.startDate, jsonEvent.endDate) ?: return null
    val rooms = jsonEvent.rooms?.map { asRoom(it) }?.filterNotNull() ?: return null
    val jsonPresenters = jsonEvent.presenters ?: return null
    return Talk(id, name, timeSlot, rooms, asSpeakers(jsonPresenters))
}

private fun asCoffeeBreakInternal(
        jsonEvent: JsonEvent,
        asTimeSlot: (String?, String?) -> TimeSlot?
): CoffeeBreak? {
    val id = jsonEvent.id ?: return null
    val name = jsonEvent.name ?: return null
    val timeSlot = asTimeSlot(jsonEvent.startDate, jsonEvent.endDate) ?: return null
    return CoffeeBreak(id, name, timeSlot)
}

private fun asPlaceholderInternal(
        jsonEvent: JsonEvent,
        asTimeSlot: (String?, String?) -> TimeSlot?
): Placeholder? {
    val id = jsonEvent.id ?: return null
    val name = jsonEvent.name ?: return null
    val timeSlot = asTimeSlot(jsonEvent.startDate, jsonEvent.endDate) ?: return null
    val rooms = jsonEvent.rooms?.map { asRoom(it) }?.filterNotNull() ?: return null
    return Placeholder(id, name, timeSlot, rooms)
}

private fun asCeremonyInternal(
        jsonEvent: JsonEvent,
        asTimeSlot: (String?, String?) -> TimeSlot?,
        asRoom: (JsonRoom?) -> Room?
): Ceremony? {
    val id = jsonEvent.id ?: return null
    val name = jsonEvent.name ?: return null
    val timeSlot = asTimeSlot(jsonEvent.startDate, jsonEvent.endDate) ?: return null
    val rooms = jsonEvent.rooms?.map { asRoom(it) }?.filterNotNull() ?: return null
    return Ceremony(id, name, timeSlot, rooms)
}

private fun asTimeSlotInternal(start: String?, end: String?, asDate: (String?) -> Date?): TimeSlot? {
    val startDate = asDate(start)
    val endDate = asDate(end)
    if (startDate == null || endDate == null) {
        return null;
    }
    return TimeSlot(startDate, endDate)
}

fun asSpearkersInternal(jsonPresenters: List<JsonPresenter>, asSpeaker: (JsonPresenter) -> Speaker?): Speakers {
    return Speakers(jsonPresenters.map { asSpeaker(it) }.filterNotNull())
}
