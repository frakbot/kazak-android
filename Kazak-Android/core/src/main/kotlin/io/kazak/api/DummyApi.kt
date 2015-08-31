package io.kazak.api

import io.kazak.model.*
import rx.Observable
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Random

public class DummyApi : KazakApi {

    val rand = Random(666)

    override fun fetchSchedule(): Observable<Schedule> {
        return generateDummyDaySchedule()
    }

    private fun generateDummyDaySchedule(): Observable<Schedule> {
        return Observable.range(0, 3)
                .flatMap {
                    getDummyDay()
                }
                .toList()
                .map {
                    Schedule(it)
                }
    }

    private fun getDummyDay(): Observable<Day> {
        return Observable.range(0, 10)
                .map {
                    Room(it.toString(), "Room ${it}")
                }
                .flatMap {
                    getDummyRoomTalks(it)
                }
                .toList()
                .map {
                    Day(Date(), it)
                }
    }

    private fun getDummyRoomTalks(room: Room): Observable<Talk> {
        val start = GregorianCalendar()
        //TODO this should not be hardcoded
        val end = GregorianCalendar(2015, Calendar.OCTOBER, 1, 10, 0)
        return Observable.range(0, 10)
                .map {
                    val unitMinutes = 15
                    val minUnits = 1
                    val maxUnits = 3
                    val n = rand.nextInt((maxUnits - minUnits) + 1) + minUnits
                    val durationMinutes = n * unitMinutes

                    start.setTime(end.getTime())
                    end.add(Calendar.MINUTE, durationMinutes)

                    val timeSlot = TimeSlot(start.getTime(), end.getTime())
                    val speaker = Speaker("${it}", "Speaker ${it}")
                    val speakers = Speakers(arrayListOf(speaker))
                    val track = Track("dummy", "Track", 0xFFBF0D7B.toInt())
                    Talk("${it}", "Talk ${it} in room ${room.name}", timeSlot, room, speakers, track)
                }
    }
}
