package io.kazak.api

import io.kazak.model.Schedule
import rx.Observable

interface KazakApi {

    fun fetchSchedule(): Observable<Schedule>

}
