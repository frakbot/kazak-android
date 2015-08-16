package io.kazak.repository

import io.kazak.api.KazakApi
import io.kazak.model.Schedule
import io.kazak.model.Talk
import io.kazak.repository.event.SyncEvent
import io.kazak.repository.event.SyncState
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject

public class KazakDataRepository(val api : KazakApi) : DataRepository {

    val scheduleCache: BehaviorSubject<Pair<Schedule?, SyncEvent>> = BehaviorSubject.create(Pair(null, SyncEvent(SyncState.IDLE, null)))

    override fun getSchedule(): Observable<Schedule> {
        if (!scheduleCache.hasValue()) {
            updateSchedule()
        }
        return scheduleCache.filter { it.first != null }.distinctUntilChanged{ it.first }.map { it.first }
    }

    override fun getScheduleSyncEvents(): Observable<SyncEvent> {
        return scheduleCache.map { it.second };
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

    // ATM there is no separate flow for talks we simply fetch them by filtering the schedule.
    // This can be revisited once a DB layer is in place.
    override fun getTalkSyncEvents(): Observable<SyncEvent> {
        return scheduleCache.map { it.second };
    }

    private fun updateSchedule() {
        scheduleCache.onNext(Pair(null, SyncEvent(SyncState.LOADING, null)))
        // TODO implement real data we get from the server
        api.fetchSchedule()
                .subscribe(ScheduleObserver(scheduleCache))
    }

    override fun bar(param: String) {
        throw UnsupportedOperationException()
    }

    class ScheduleObserver(val subject: BehaviorSubject<Pair<Schedule?, SyncEvent>>) : Observer<Schedule> {

        override fun onCompleted() {
            subject.onNext(Pair(subject.getValue().first, SyncEvent(SyncState.IDLE, null)))
        }

        override fun onError(e: Throwable) {
            subject.onNext(Pair(subject.getValue().first, SyncEvent(SyncState.ERROR, e)))
        }

        override fun onNext(t: Schedule) {
            subject.onNext(Pair(t, SyncEvent(SyncState.IDLE, null)))
        }

    }

}
