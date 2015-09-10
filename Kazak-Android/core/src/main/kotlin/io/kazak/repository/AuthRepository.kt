package io.kazak.repository

import io.kazak.auth.KazakAuthToken
import io.kazak.repository.event.SyncEvent
import rx.Observable

public interface AuthRepository {

    fun login(username: String, password: String)

    fun clearLoginCache()

    fun forceIdleState()

    fun getLastLoginSyncEvents(): Observable<SyncEvent>

    fun getLoginCache(): Observable<KazakAuthToken>

}
