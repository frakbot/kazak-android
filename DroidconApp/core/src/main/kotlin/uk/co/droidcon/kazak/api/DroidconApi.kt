package uk.co.droidcon.kazak.api

import rx.Observable
import uk.co.droidcon.kazak.model.Schedule
import uk.co.droidcon.kazak.model.Talk

public interface DroidconApi {

    fun fetchSchedule(): Observable<Schedule>

}
