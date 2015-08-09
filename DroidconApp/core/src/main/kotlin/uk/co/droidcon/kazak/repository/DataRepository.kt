package uk.co.droidcon.kazak.repository

import rx.Observable
import uk.co.droidcon.kazak.model.Schedule
import uk.co.droidcon.kazak.model.Talk

public interface DataRepository {

    fun getSchedule(): Observable<Schedule>

    fun getScheduleSyncEvents(): Observable<SyncEvent>

    fun getTalk(id : String): Observable<Talk>

    fun bar(param : String)

}
