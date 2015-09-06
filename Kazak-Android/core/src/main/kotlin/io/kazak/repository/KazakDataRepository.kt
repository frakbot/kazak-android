package io.kazak.repository

import io.kazak.api.KazakApi
import io.kazak.model.EventType
import io.kazak.model.Schedule
import io.kazak.model.Talk
import io.kazak.repository.event.SyncEvent
import io.kazak.repository.event.SyncEventObserver
import io.kazak.repository.event.SyncState
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject

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
            Observable.from(it.events)
        }.filter {
            it.type() == EventType.TALK
        }.map{
            it as Talk
        }.filter {
            it.id == id
        }
    }

    // ATM there is no separate flow for talks we simply fetch them by filtering the schedule.
    // This can be revisited once a DB layer is in place.
    override fun getTalkSyncEvents(): Observable<SyncEvent> {
        return scheduleSyncCache;
    }

    private fun updateSchedule() {
        scheduleSyncCache.onNext(SyncEvent(SyncState.LOADING, null))
        // TODO implement real data we get from the server
        api.fetchSchedule()
                .subscribe(SyncEventObserver(scheduleCache, scheduleSyncCache))
    }

    override fun bar(param: String) {
        throw UnsupportedOperationException()
    }

}
