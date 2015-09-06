package io.kazak.auth

import io.kazak.model.Schedule
import rx.Observable

public interface KazakAuth {

    fun login(username: String, password: String) : Observable<String>

}
