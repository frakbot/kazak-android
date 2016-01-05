package io.kazak.api

import io.kazak.api.json.model.JsonEvent
import io.kazak.api.json.model.JsonPresenter
import io.kazak.api.json.model.JsonRoom
import io.kazak.api.json.model.JsonTrack
import io.kazak.model.*
import io.kazak.utils.SimpleDateFormatThreadSafe
import java.text.ParseException
import java.util.*

private val ISO_DATE_FORMATTER = SimpleDateFormatThreadSafe("yyyy-MM-dd'T'HH:mm:ss")

private val HEX_COLOR_PREFIX = '#'
private val RGB_HEX_LENGTH = 7
private val ARGB_HEX_LENGTH = 9
private val FULL_ALPHA_BITMASK = 0x00000000ff000000
private val HEX_BASE_16_RADIX = 16

fun asSchedule(jsonTalks: List<JsonEvent>) = asScheduleInternal(jsonTalks, ::asEvent)

fun asEvent(jsonEvent: JsonEvent) = asEventInternal(jsonEvent, ::asTalk, ::asCoffeeBreak, ::asCeremony, ::asPlaceholder)

fun asTalk(jsonEvent: JsonEvent) = asTalkInternal(jsonEvent, ::asTimeSlot, ::asRoom, ::asSpeakers, ::asTrack)

fun asCoffeeBreak(jsonEvent: JsonEvent) = asCoffeeBreakInternal(jsonEvent, ::asTimeSlot)

fun asPlaceholder(jsonEvent: JsonEvent) = asPlaceholderInternal(jsonEvent, ::asTimeSlot)

fun asCeremony(jsonEvent: JsonEvent) = asCeremonyInternal(jsonEvent, ::asTimeSlot, ::asRoom)

fun asTimeSlot(start: String?, end: String?) = asTimeSlotInternal(start, end, ::asDate)

fun asTrack(jsonTrack: JsonTrack?) = asTrackInternal(jsonTrack, ::asColor)

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
    return Room(Id(id), name)
}

fun asSpeakers(jsonPresenters: List<JsonPresenter>): Speakers = asSpeakersInternal(jsonPresenters, ::asSpeaker)

fun asSpeaker(jsonPresenter: JsonPresenter): Speaker? {
    val id = jsonPresenter.id ?: return null
    val name = jsonPresenter.name ?: return null
    return Speaker(
            Id(id),
            name,
            jsonPresenter.company,
            jsonPresenter.bio,
            jsonPresenter.pic,
            jsonPresenter.social
    )
}

private fun asScheduleInternal(jsonTalks: List<JsonEvent>, asEvent: (JsonEvent) -> Event?): Schedule {
    val calendar = Calendar.getInstance()
    val talks = jsonTalks.map {
        asEvent(it)
    }.filterNotNull().groupBy {
        calendar.time = it.timeSlot().start
        calendar.get(Calendar.DAY_OF_YEAR)
    }.map {
        val date = it.value.get(0).timeSlot().start // TODO: improve this
        Day(date, it.value)
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
        asSpeakers: (List<JsonPresenter>) -> Speakers,
        asTrack: (JsonTrack?) -> Track?
): Talk? {
    val id = jsonEvent.id ?: return null
    val name = jsonEvent.name ?: return null
    val description = jsonEvent.description
    val timeSlot = asTimeSlot(jsonEvent.startDate, jsonEvent.endDate) ?: return null
    val rooms = jsonEvent.rooms?.map { asRoom(it) }?.filterNotNull() ?: return null
    val jsonPresenters = jsonEvent.presenters ?: return null
    return Talk(Id(id), name, description, timeSlot, rooms, asSpeakers(jsonPresenters), asTrack(jsonEvent.track))
}

private fun asCoffeeBreakInternal(
        jsonEvent: JsonEvent,
        asTimeSlot: (String?, String?) -> TimeSlot?
): CoffeeBreak? {
    val id = jsonEvent.id ?: return null
    val name = jsonEvent.name ?: return null
    val description = jsonEvent.description
    val timeSlot = asTimeSlot(jsonEvent.startDate, jsonEvent.endDate) ?: return null
    return CoffeeBreak(Id(id), name, description, timeSlot)
}

private fun asPlaceholderInternal(
        jsonEvent: JsonEvent,
        asTimeSlot: (String?, String?) -> TimeSlot?
): Placeholder? {
    val id = jsonEvent.id ?: return null
    val name = jsonEvent.name ?: return null
    val description = jsonEvent.description
    val timeSlot = asTimeSlot(jsonEvent.startDate, jsonEvent.endDate) ?: return null
    val rooms = jsonEvent.rooms?.map { asRoom(it) }?.filterNotNull() ?: return null
    return Placeholder(Id(id), name, description, timeSlot, rooms)
}

private fun asCeremonyInternal(
        jsonEvent: JsonEvent,
        asTimeSlot: (String?, String?) -> TimeSlot?,
        asRoom: (JsonRoom?) -> Room?
): Ceremony? {
    val id = jsonEvent.id ?: return null
    val name = jsonEvent.name ?: return null
    val description = jsonEvent.description
    val timeSlot = asTimeSlot(jsonEvent.startDate, jsonEvent.endDate) ?: return null
    val rooms = jsonEvent.rooms?.map { asRoom(it) }?.filterNotNull() ?: return null
    return Ceremony(Id(id), name, description, timeSlot, rooms)
}

private fun asTimeSlotInternal(start: String?, end: String?, asDate: (String?) -> Date?): TimeSlot? {
    val startDate = asDate(start)
    val endDate = asDate(end)
    if (startDate == null || endDate == null) {
        return null
    }
    return TimeSlot(startDate, endDate)
}

fun asSpeakersInternal(jsonPresenters: List<JsonPresenter>, asSpeaker: (JsonPresenter) -> Speaker?): Speakers {
    return Speakers(jsonPresenters.map { asSpeaker(it) }.filterNotNull())
}

fun asTrackInternal(jsonTrack: JsonTrack?, asColor: (String?) -> Color?): Track? {
    if (jsonTrack == null) {
        return null
    }
    val id = jsonTrack.id ?: return null
    val name = jsonTrack.name ?: return null
    return Track(Id(id), name, asColor(jsonTrack.color))
}

fun asColor(hexColorString: String?): Color? {
    if (hexColorString == null) {
        return null
    }

    val trimmedHexColorString = hexColorString.trim()
    if (notValid(trimmedHexColorString) || unsupportedFormat(trimmedHexColorString)) {
        return null
    }

    if (trimmedHexColorString.length == RGB_HEX_LENGTH) {
        return Color(parseRgbHex(trimmedHexColorString).toInt())
    } else {
        return Color(parseArgbHex(trimmedHexColorString).toInt())
    }
}

private fun notValid(hexColorString: String): Boolean {
    return hexColorString.isEmpty() || hexColorString[0] != HEX_COLOR_PREFIX
}

private fun unsupportedFormat(hexColorString: String): Boolean {
    return hexColorString.length != RGB_HEX_LENGTH && hexColorString.length != ARGB_HEX_LENGTH
}

private fun parseRgbHex(rgbHex: String): Long {
    return parseArgbHex(rgbHex).or(FULL_ALPHA_BITMASK)
}

private fun parseArgbHex(argbHex: String): Long {
    return java.lang.Long.parseLong(argbHex.substring(1), HEX_BASE_16_RADIX)
}
