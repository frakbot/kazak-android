package com.droidcon.uk.app

import com.droidcon.uk.app.model.Schedule
import com.droidcon.uk.app.model.Talk
import rx.Observable

public interface DataRepository {
    fun getSchedule(): Observable<Schedule>

    fun getTalk(id : String): Observable<Talk>

    fun bar(param : String)
}
