package io.kazak.repository

import io.kazak.model.Event
import io.kazak.model.Id
import io.kazak.model.Schedule
import io.kazak.model.Session
import io.kazak.repository.event.SyncEvent
import rx.Observable

public interface DataRepository {

    fun getSchedule(): Observable<Schedule>

    fun getScheduleSyncEvents(): Observable<SyncEvent>

    fun refreshSchedule() : Observable<SyncEvent>

    fun getFavoriteIds(): Observable<List<Id>>

    fun getFavorites(): Observable<List<Session>>

    fun addToFavorites(id: Id)

    fun removeFromFavorites(id: Id)

    fun getFavoritesSyncEvents(): Observable<SyncEvent>

    fun getEvent(id : Id): Observable<Event>

    fun getTalkSyncEvents(): Observable<SyncEvent>

}
