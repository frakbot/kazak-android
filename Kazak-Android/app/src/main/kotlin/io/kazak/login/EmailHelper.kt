package io.kazak.login

import android.accounts.AccountManager
import android.content.Context

public fun getDeviceUserEmails(context: Context): List<String> {
    // fetch and filter emails from the account manager
    val am = AccountManager.get(context)
    val accounts = am.getAccounts().toList();
    return accounts
            .map {
                it.name
            }
            .filter {
                isValidEmail(it)
            }
            .distinct()
}

public fun isValidEmail(email: String): Boolean {
    // TODO: Replace this with an improved logic
    return email.contains("@")
}
