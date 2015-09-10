package io.kazak.repository

import io.kazak.api.KazakApi
import io.kazak.model.*
import io.kazak.repository.event.SyncEvent
import io.kazak.repository.event.SyncState
import rx.Observable
import rx.Observer
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

public class KazakDataRepository(val api: KazakApi, val favoritesRepository: FavoriteSessionsRepository) : DataRepository {

    val scheduleCache: BehaviorSubject<Schedule> = BehaviorSubject.create()
    val scheduleSyncCache: BehaviorSubject<SyncEvent> = BehaviorSubject.create()

    val favoritesCache: BehaviorSubject<FavoriteSessions> = BehaviorSubject.create()
    val favoritesSyncCache: BehaviorSubject<SyncEvent> = BehaviorSubject.create()

    override fun getSchedule(): Observable<Schedule> {
        if (!scheduleCache.hasValue()) {
            updateSchedule()
        }
        return scheduleCache
    }

    override fun getScheduleSyncEvents(): Observable<SyncEvent> {
        return scheduleSyncCache;
    }

    override fun getFavoriteIds(): Observable<List<Id>> {
        return getFavoritesStatuses()
                .flatMap {
                    Observable.from(it.statuses.entrySet())
                            .filter { it.getValue() == FavoriteStatus.FAVORITE }
                            .map { it.getKey() }
                            .toList()
                }
    }

    override fun getFavorites(): Observable<List<Session>> {
        return getFavoriteIds()
                .flatMap {
                    Observable.from(it)
                        .flatMap { getEvent(it) }
                        .filter { it.type().canBeFavorite() }
                        .cast(javaClass<Session>())
                        .toList()
                }
    }

    private fun getFavoritesStatuses(): Observable<FavoriteSessions> {
        if (!favoritesCache.hasValue()) {
            updateFavorites()
        }
        return favoritesCache
    }

    private fun updateFavorites() {
        favoritesSyncCache.onNext(SyncEvent(SyncState.LOADING, null))
        favoritesRepository.read()
                .subscribeOn(Schedulers.io())
                .subscribe(SyncObserver(favoritesCache, favoritesSyncCache))
    }

    override fun getFavoritesSyncEvents(): Observable<SyncEvent> {
        return favoritesSyncCache;
    }

    override fun addToFavorites(id: Id) {
        getFavoritesStatuses()
                .map { it.statuses.plus(Pair(id, FavoriteStatus.FAVORITE)) }
                .map { FavoriteSessions(it) }
                .doOnNext { favoritesRepository.store(it).subscribe() }
                .subscribe(SyncObserver(favoritesCache, favoritesSyncCache))
    }

    override fun removeFromFavorites(id: Id) {
        getFavoritesStatuses()
                .map { it.statuses.minus(id) }
                .map { FavoriteSessions(it) }
                .doOnNext { favoritesRepository.store(it).subscribe() }
                .subscribe(SyncObserver(favoritesCache, favoritesSyncCache))
    }

    override fun getEvent(id: Id): Observable<Event> {
        return getSchedule().flatMap {
            Observable.from(it.days)
        }.flatMap {
            Observable.from(it.events)
        }.filter {
            it.id() == id
        }
    }

    // ATM there is no separate flow for talks we simply fetch them by filtering the schedule.
    // This can be revisited once a DB layer is in place.
    override fun getTalkSyncEvents(): Observable<SyncEvent> {
        return scheduleSyncCache;
    }

    private fun updateSchedule() {
        scheduleSyncCache.onNext(SyncEvent(SyncState.LOADING, null))
        api.fetchSchedule()
                .subscribe(SyncObserver(scheduleCache, scheduleSyncCache))
    }

    class SyncObserver<T>(val subject: BehaviorSubject<T>, val syncSubject: BehaviorSubject<SyncEvent>) : Observer<T> {

        override fun onCompleted() {
            syncSubject.onNext(SyncEvent(SyncState.IDLE, null))
        }

        override fun onError(e: Throwable) {
            syncSubject.onNext(SyncEvent(SyncState.ERROR, e))
        }

        override fun onNext(t: T) {
            subject.onNext(t)
            syncSubject.onNext(SyncEvent(SyncState.IDLE, null))
        }

    }

}
