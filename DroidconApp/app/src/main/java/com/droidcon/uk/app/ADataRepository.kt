package com.droidcon.uk.app

import com.droidcon.uk.app.model.Schedule
import com.droidcon.uk.app.model.Talk
import com.droidcon.uk.app.rx.InfiniteOperator
import rx.Observable
import rx.subjects.BehaviorSubject
import java.util.Collections

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
        Observable.just(Schedule(Collections.emptyList()))
                .lift(InfiniteOperator<Schedule>())
                .subscribe(scheduleCache)
    }

    override fun bar(param: String) {
        throw UnsupportedOperationException()
    }

}
