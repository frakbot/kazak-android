package io.kazak.repository

import io.kazak.model.Schedule
import io.kazak.model.Talk
import io.kazak.repository.event.SyncEvent
import rx.Observable

public interface DataRepository {

    fun getSchedule(): Observable<Schedule>

    fun getScheduleSyncEvents(): Observable<SyncEvent>

    fun getTalk(id : String): Observable<Talk>

    fun bar(param : String)

}
