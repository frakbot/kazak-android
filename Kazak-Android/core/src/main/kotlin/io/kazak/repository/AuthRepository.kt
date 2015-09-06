package io.kazak.repository

import io.kazak.repository.event.SyncEvent
import rx.Observable

public interface AuthRepository {

    fun login(username: String, password: String)

    fun clearLoginCache()

    fun getLastLoginSyncEvents(): Observable<SyncEvent>

    fun getLoginCache(): Observable<String>

}
