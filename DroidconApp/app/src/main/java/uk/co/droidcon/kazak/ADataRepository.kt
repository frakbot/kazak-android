package uk.co.droidcon.kazak

import rx.Observable
import rx.subjects.BehaviorSubject
import uk.co.droidcon.kazak.model.*
import uk.co.droidcon.kazak.rx.InfiniteOperator
import java.util.Date

public class ADataRepository : DataRepository {

    val scheduleCache: BehaviorSubject<Schedule> = BehaviorSubject.create()

    override fun getSchedule(): Observable<Schedule> {
        if (!scheduleCache.hasValue()) {
            updateSchedule()
        }
        return scheduleCache
    }

    override fun getTalk(id: String): Observable<Talk> {
        return scheduleCache.flatMap {
            Observable.from(it.days)
        }.flatMap {
            Observable.from(it.talks)
        }.filter {
            it.id == id
        }
    }

    private fun updateSchedule() {
        // TODO implement real data we get from the server
        generateDummyDaySchedule()
                .lift(InfiniteOperator<Schedule>())
                .subscribe(scheduleCache)
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

    override fun bar(param: String) {
        throw UnsupportedOperationException()
    }

}
