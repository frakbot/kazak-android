package io.kazak.api

import rx.Observable
import io.kazak.model.Schedule
import io.kazak.model.Talk

public interface KazakApi {

    fun fetchSchedule(): Observable<Schedule>

}
