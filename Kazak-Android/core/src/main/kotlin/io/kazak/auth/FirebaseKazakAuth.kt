package io.kazak.auth

import com.firebase.client.AuthData
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import rx.Observable
import rx.schedulers.Schedulers

public class FirebaseKazakAuth(val baseUrl: String) : KazakAuth {

    override fun login(username: String, password: String): Observable<KazakAuthToken> {
        val ref = Firebase(baseUrl)
        return Observable.create<KazakAuthToken> {
            ref.authWithPassword(username, password, object : Firebase.AuthResultHandler {
                override fun onAuthenticationError(error: FirebaseError?) {
                    it.onError(UnauthorizedException(error?.getMessage()))
                }

                override fun onAuthenticated(authData: AuthData?) {
                    if (authData != null) {
                        it.onNext(KazakAuthToken(authData.getToken()))
                        it.onCompleted()
                        return
                    }
                    it.onError(UnauthorizedException("Login returned an empty auth token"))
                }
            })
        }.subscribeOn(Schedulers.io())
    }

}
