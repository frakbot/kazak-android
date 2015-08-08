package io.kazak.repository

import rx.Observable
import rx.subjects.BehaviorSubject
import io.kazak.api.KazakApi
import io.kazak.model.*
import io.kazak.rx.InfiniteOperator
import java.util.Date

public class KazakDataRepository(val api : KazakApi) : DataRepository {

    val scheduleCache: BehaviorSubject<Schedule> = BehaviorSubject.create()

    override fun getSchedule(): Observable<Schedule> {
        if (!scheduleCache.hasValue()) {
            updateSchedule()
        }
        return scheduleCache
    }

    override fun getTalk(id: String): Observable<Talk> {
        return getSchedule().flatMap {
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
