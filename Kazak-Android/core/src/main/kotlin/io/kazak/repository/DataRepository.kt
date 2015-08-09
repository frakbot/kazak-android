package io.kazak.repository

import io.kazak.model.Schedule
import io.kazak.model.Talk
import rx.Observable
import uk.co.droidcon.kazak.repository.SyncEvent

public interface DataRepository {

    fun getSchedule(): Observable<Schedule>

    fun getScheduleSyncEvents(): Observable<SyncEvent>

    fun getTalk(id : String): Observable<Talk>

    fun bar(param : String)

}
