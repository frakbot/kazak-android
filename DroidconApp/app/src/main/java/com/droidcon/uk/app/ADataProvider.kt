package com.droidcon.uk.app

import rx.Observable
import rx.subjects.BehaviorSubject

public class ADataProvider : DataProvider {

    val scheduleCache: BehaviorSubject<Schedule> = BehaviorSubject.create()

    override fun getSchedule(): Observable<Schedule> {
        if (!scheduleCache.hasValue()) {
            fetchSchedule()
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

    private fun fetchSchedule() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bar(param: String) {
        throw UnsupportedOperationException()
    }

}
