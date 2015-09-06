package io.kazak.repository

import io.kazak.model.FavoriteSessions
import io.kazak.model.Id
import io.kazak.model.Schedule
import io.kazak.model.Talk
import io.kazak.repository.event.SyncEvent
import rx.Observable

public interface DataRepository {

    fun getSchedule(): Observable<Schedule>

    fun getScheduleSyncEvents(): Observable<SyncEvent>

    fun getFavorites(): Observable<List<Id>>

    fun addToFavorites(id: Id)

    fun removeFromFavorites(id: Id)

    fun getFavoritesSyncEvents(): Observable<SyncEvent>

    fun getTalk(id : Id): Observable<Talk>

    fun getTalkSyncEvents(): Observable<SyncEvent>

}
