package io.kazak.repository

import io.kazak.api.KazakApi
import io.kazak.model.Schedule
import io.kazak.model.Talk
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import uk.co.droidcon.kazak.repository.SyncEvent
import io.kazak.repository.event.SyncState

public class KazakDataRepository(val api : KazakApi) : DataRepository {

    val scheduleCache: BehaviorSubject<Schedule> = BehaviorSubject.create()
    val scheduleSyncCache: BehaviorSubject<SyncEvent> = BehaviorSubject.create()

    override fun getSchedule(): Observable<Schedule> {
        if (!scheduleCache.hasValue()) {
            updateSchedule()
        }
        return scheduleCache
    }

    override fun getScheduleSyncEvents(): Observable<SyncEvent> {
        return scheduleSyncCache;
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
        scheduleSyncCache.onNext(SyncEvent(SyncState.LOADING, null))
        // TODO implement real data we get from the server
        api.fetchSchedule()
                .subscribe(ScheduleObserver(scheduleCache, scheduleSyncCache))
    }

    override fun bar(param: String) {
        throw UnsupportedOperationException()
    }

    class ScheduleObserver(val subject: BehaviorSubject<Schedule>, val syncSubject: BehaviorSubject<SyncEvent>) : Observer<Schedule> {

        override fun onCompleted() {
            syncSubject.onNext(SyncEvent(SyncState.IDLE, null))
        }

        override fun onError(e: Throwable) {
            syncSubject.onNext(SyncEvent(SyncState.ERROR, e))
        }

        override fun onNext(t: Schedule) {
            subject.onNext(t)
            syncSubject.onNext(SyncEvent(SyncState.IDLE, null))
        }

    }

}
