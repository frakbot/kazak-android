package io.kazak.login;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

public class KazakAuthenticator extends AbstractAccountAuthenticator {
    private Context context;

    public KazakAuthenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
                             String accountType,
                             String authTokenType,
                             String[] requiredFeatures,
                             Bundle options) throws NetworkErrorException {
        // build and return an intent that will fire up the login activity for a new account
        return getLoginBundledIntent(response, accountType, authTokenType, true);
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        // no need to confirm credentials
        return null;
    }

    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse response,
            Account account,
            String authTokenType,
            Bundle options) throws NetworkErrorException {
        // retrieve the already-stored auth token
        AccountManager am = AccountManager.get(context);
        String authToken = am.peekAuthToken(account, authTokenType);

        // if there's no stored token, return the bundled Intent to launch the login activity
        if (TextUtils.isEmpty(authToken)) {
            return getLoginBundledIntent(response, account.type, authTokenType, false);
        }

        // if there is a token stored, return it
        final Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        return result;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // there's only one kind of auth token, no need to label it
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        // no need to update credentials
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        // no features attached
        return null;
    }

    /**
     * Create an {@link Intent} to open the {@link LoginActivity} with the proper extras set into it.
     *
     * @param response      The response object that will be used by the login activity to write information into.
     * @param accountType   The type of the account
     * @param authTokenType The type of the auth token
     * @param isNew         {@code true} if the login was triggered by the "new account" option in the settings, {@code false} otherwise
     * @return An {@link Intent} to launch the {@link LoginActivity}
     */
    private Intent createIntentLogin(AccountAuthenticatorResponse response, String accountType, String authTokenType, boolean isNew) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(LoginActivity.AUTH_TOKEN_TYPE, authTokenType);
        intent.putExtra(LoginActivity.NEW_ACCOUNT, isNew);
        return intent;
    }

    private Bundle getLoginBundledIntent(AccountAuthenticatorResponse response, String accountType, String authTokenType, boolean isNew) {
        Intent intent = createIntentLogin(response, accountType, authTokenType, isNew);
        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }
}
