package uk.co.droidcon.kazak

import rx.Observable
import rx.subjects.BehaviorSubject
import uk.co.droidcon.kazak.model.*
import uk.co.droidcon.kazak.rx.InfiniteOperator
import java.util.ArrayList
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
        Observable.just(Schedule(generateDummyDaySchedule()))
                .lift(InfiniteOperator<Schedule>())
                .subscribe(scheduleCache)
    }

    private fun generateDummyDaySchedule(): List<Day> {
        val talks = generateDummyTalks()
        val day = Day(Date(), talks)
        val list = ArrayList<Day>(1)
        list.add(day)
        return list
    }

    private fun generateDummyTalks(): List<Talk> {
        val talks = ArrayList<Talk>(10)
        val room = Room("main", "Main room")
        for (i in 0..9) {
            val timeSlot = TimeSlot(Date(), Date())
            talks.add(Talk("${i}", "Talk ${i}", timeSlot, room))
        }
        return talks
    }

    override fun bar(param: String) {
        throw UnsupportedOperationException()
    }

}
