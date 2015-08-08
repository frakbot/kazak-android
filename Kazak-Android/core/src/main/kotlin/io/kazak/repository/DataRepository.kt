package io.kazak.repository

import io.kazak.model.Schedule
import io.kazak.model.Talk
import rx.Observable

public interface DataRepository {

    fun getSchedule(): Observable<Schedule>

    fun getTalk(id : String): Observable<Talk>

    fun bar(param : String)

}
