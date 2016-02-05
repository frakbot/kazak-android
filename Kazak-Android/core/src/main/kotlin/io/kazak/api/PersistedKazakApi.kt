package io.kazak.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.kazak.api.json.JsonKazakApi
import io.kazak.api.json.model.JsonEvent
import io.kazak.model.Schedule
import io.kazak.repository.JsonRepository
import rx.Observable
import rx.schedulers.Schedulers

class PersistedKazakApi(val remoteApi: JsonKazakApi, val jsonRepository: JsonRepository, val gson: Gson) : KazakApi {

    override fun fetchSchedule(): Observable<Schedule> {
        return Observable.concat(fetchLocalSchedule(), fetchRemoteSchedule())
                .map { toJsonEvents(it) }
                .map { asSchedule(it) }
    }

    private fun fetchLocalSchedule(): Observable<String> {
        return jsonRepository.read()
                .subscribeOn(Schedulers.io())
    }

    private fun fetchRemoteSchedule(): Observable<String> {
        return remoteApi.fetchSchedule()
                .map { it.toString() }
                .doOnNext { jsonRepository.store(it).subscribe() }
                .subscribeOn(Schedulers.io())
    }

    private fun toJsonEvents(it: String): List<JsonEvent> = gson.fromJson(it, object : TypeToken<List<JsonEvent>>() {}.type)

}
