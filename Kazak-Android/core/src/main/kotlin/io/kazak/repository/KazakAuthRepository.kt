package io.kazak.repository

import io.kazak.auth.KazakAuth
import io.kazak.auth.KazakAuthToken
import io.kazak.repository.event.SyncEvent
import io.kazak.repository.event.SyncObserver
import rx.Observable
import rx.subjects.BehaviorSubject

public class KazakAuthRepository(val authenticator: KazakAuth) : AuthRepository {

    var authTokenCache: BehaviorSubject<KazakAuthToken> = BehaviorSubject.create()
    var authTokenSyncCache: BehaviorSubject<SyncEvent> = BehaviorSubject.create()

    override fun login(username: String, password: String) {
            authenticator.login(username, password)
                    .subscribe(SyncObserver(authTokenCache, authTokenSyncCache))
    }

    override fun clearLoginCache() {
        authTokenCache = BehaviorSubject.create()
        authTokenSyncCache = BehaviorSubject.create()
    }

    override fun getLoginCache(): Observable<KazakAuthToken> {
        return authTokenCache
    }

    override fun getLastLoginSyncEvents(): Observable<SyncEvent> {
        return authTokenSyncCache
    }

}
