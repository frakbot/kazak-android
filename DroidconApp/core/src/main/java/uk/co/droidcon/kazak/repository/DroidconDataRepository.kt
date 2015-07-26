package uk.co.droidcon.kazak.repository

import rx.Observable
import rx.subjects.BehaviorSubject
import uk.co.droidcon.kazak.api.DroidconApi
import uk.co.droidcon.kazak.model.*
import uk.co.droidcon.kazak.rx.InfiniteOperator
import java.util.Date

public class DroidconDataRepository(val api : DroidconApi) : DataRepository {

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
        api.fetchSchedule()
                .lift(InfiniteOperator<Schedule>())
                .subscribe(scheduleCache)
    }

    override fun bar(param: String) {
        throw UnsupportedOperationException()
    }

}
