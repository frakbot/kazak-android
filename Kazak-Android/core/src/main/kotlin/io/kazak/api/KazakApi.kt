package io.kazak.api

import io.kazak.model.Schedule
import rx.Observable

public interface KazakApi {

    fun fetchSchedule(): Observable<Schedule>

}
