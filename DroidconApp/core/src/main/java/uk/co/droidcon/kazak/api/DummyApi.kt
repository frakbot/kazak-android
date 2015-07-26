package uk.co.droidcon.kazak.api

import rx.Observable
import uk.co.droidcon.kazak.model.*
import java.util.*

public class DummyApi : DroidconApi {

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
        return Observable.range(0, 4)
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
        return Observable.range(0, 10)
                .map {
                    val timeSlot = TimeSlot(Date(), Date())
                    val speaker = Speaker("${it}", "Speaker ${it}")
                    val speakers = Speakers(arrayListOf(speaker))
                    Talk("${it}", "Talk ${it} in room ${room.name}", timeSlot, room, speakers)
                }
    }
}
