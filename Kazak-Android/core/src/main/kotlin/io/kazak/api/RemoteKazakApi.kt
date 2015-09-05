package io.kazak.api

import io.kazak.api.json.JsonKazakApi
import io.kazak.api.asSchedule
import io.kazak.model.Schedule
import rx.Observable
import rx.schedulers.Schedulers

public class RemoteKazakApi(val remoteApi: JsonKazakApi) : KazakApi {

    override fun fetchSchedule(): Observable<Schedule> {
        return remoteApi.fetchSchedule()
                .map {
                    asSchedule(it)
                }.subscribeOn(Schedulers.io())
    }

}
