package io.kazak.repository

import rx.Observable
import io.kazak.model.Schedule
import io.kazak.model.Talk

public interface DataRepository {

    fun getSchedule(): Observable<Schedule>

    fun getTalk(id : String): Observable<Talk>

    fun bar(param : String)

}
