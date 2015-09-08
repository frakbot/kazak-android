package io.kazak.login

import android.accounts.AccountManager
import android.content.Context
import rx.Observable

public fun getDeviceUserEmails(context: Context): List<String> {
    // fetch and filter emails from the account manager
    val am = AccountManager.get(context)
    val accounts = am.getAccounts()
    return Observable.from(accounts)
            .map<String> {
                it.name
            }
            .filter {
                isEmailValid(it)
            }
            .distinct()
            .toList()
            .toBlocking()
            .single()
}

public fun isEmailValid(email: String): Boolean {
    // TODO: Replace this with an improved logic
    return email.contains("@")
}
