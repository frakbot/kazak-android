package com.droidcon.uk.app

import rx.Observable

public interface DataProvider  {
    fun getSchedule(): Observable<Schedule>

    fun getTalk(id : String): Observable<Talk>

    fun bar(param : String)
}
