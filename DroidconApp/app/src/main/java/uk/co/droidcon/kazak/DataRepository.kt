package uk.co.droidcon.kazak

import uk.co.droidcon.kazak.model.Schedule
import uk.co.droidcon.kazak.model.Talk
import rx.Observable

public interface DataRepository {

    fun getSchedule(): Observable<Schedule>

    fun getTalk(id : String): Observable<Talk>

    fun bar(param : String)

}
