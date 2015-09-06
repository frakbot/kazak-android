package io.kazak.login;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.Firebase;

import javax.inject.Inject;
import java.util.List;

import io.kazak.KazakApplication;
import io.kazak.R;
import io.kazak.repository.AuthRepository;
import io.kazak.repository.event.SyncEvent;
import io.kazak.repository.event.SyncState;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    // unused for now, can be used to discriminate whether this is a new login or a confirmation for an existing one
    public static final String NEW_ACCOUNT = "NEW_ACCOUNT";
    // the token type, specifies the kind of login to perform (credentials, google, facebook, twitter, ..)
    public static final String AUTH_TOKEN_TYPE = "AUTH_TOKEN_TYPE";
    // default token type (username and password)
    private static final String AUTH_TOKEN_TYPE_PASSWORD = "AUTH_TOKEN_TYPE_PASSWORD";
    // bundle state key, if its value is true the login cache will be invalidated when the activity is created
    private static final String BUNDLE_NEED_NEW_CACHE = "BUNDLE_NEW_CACHE";
    // bundle key, its value specifies that a login is in progress
    private static final String BUNDLE_LOGIN_IN_PROGRESS = "BUNDLE_LOGIN_IN_PROGRESS";

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Inject
    AuthRepository authRepository;
    CompositeSubscription loginSubscriptions;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private boolean isLoggingIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KazakApplication.injector().inject(this);

        setContentView(R.layout.activity_login);

        // Init Firebase
        Firebase.setAndroidContext(this);

        // if this is a new login session, clear the login cache
        if (savedInstanceState == null || !savedInstanceState.getBoolean(BUNDLE_NEED_NEW_CACHE)) {
            Log.v(TAG, "New LoginActivity created, clearing the login cache.");
            authRepository.clearLoginCache();
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        // Handle enter on the password
        mPasswordView.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                }
        );

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                }
        );

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // will hold two subscriptions
        loginSubscriptions = new CompositeSubscription();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unsubscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscribe();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(BUNDLE_LOGIN_IN_PROGRESS, isLoggingIn);
        outState.putBoolean(BUNDLE_NEED_NEW_CACHE, true);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        showProgress(savedInstanceState.getBoolean(BUNDLE_LOGIN_IN_PROGRESS));
    }

    private void subscribe() {
        // Subscribe to the cache and sync state cache
        loginSubscriptions.add(authRepository.getLastLoginSyncEvents().subscribe(new OnAuthTokenErroredAction()));
        loginSubscriptions.add(authRepository.getLoginCache().subscribe(new OnAuthTokenRetrievedAction(this)));
        Log.v(TAG, "Subscribed to login events.");
    }

    private void unsubscribe() {
        // unsubscribe from all the things
        loginSubscriptions.unsubscribe();
        loginSubscriptions = new CompositeSubscription();
        Log.v(TAG, "Unsubscribed from login events.");
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        unsubscribe();
        authRepository.clearLoginCache();
        subscribe();

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            hideKeyboard();
            showProgress(true);
            isLoggingIn = true;
            authRepository.login(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with an improved logic
        return email.contains("@");
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1
            ).setListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    }
            );

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0
            ).setListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    }
            );
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void populateAutoComplete() {
        // fetch and filter emails from the account manager
        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccounts();
        List<String> emails = Observable.from(accounts)
                .map(new MapAccountToName())
                .filter(new FilterEmail())
                .distinct()
                .toList()
                .toBlocking()
                .single();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emails
                );

        mEmailView.setAdapter(adapter);
    }

    private class MapAccountToName implements Func1<Account, String> {
        @Override
        public String call(Account account) {
            return account.name;
        }
    }

    private class FilterEmail implements Func1<String, Boolean> {
        @Override
        public Boolean call(String account) {
            return isEmailValid(account);
        }
    }

    private class OnAuthTokenRetrievedAction implements Action1<String> {
        private final LoginActivity activity;

        public OnAuthTokenRetrievedAction(LoginActivity activity) {
            this.activity = activity;
        }

        @Override
        public void call(String authToken) {
            Log.v(TAG, "Got new auth token, saving the credentials in the AccountManager...");
            showProgress(false);
            Intent originalIntent = getIntent();
            String accountName = mEmailView.getText().toString();
            final Account account = new Account(accountName, originalIntent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
            String authTokenType = originalIntent.getStringExtra(AUTH_TOKEN_TYPE);
            if (authTokenType == null) {
                authTokenType = AUTH_TOKEN_TYPE_PASSWORD;
            }
            // Explicitly add an account with null password
            final AccountManager mAccountManager = AccountManager.get(activity);
            mAccountManager.addAccountExplicitly(account, null, null);
            // Save the token credentials in the AccountManager
            mAccountManager.setAuthToken(account, authTokenType, authToken);
            setResult(RESULT_OK, originalIntent);
            Log.v(TAG, "Auth token saved in the AccountManager.");
            finish();
        }
    }

    private class OnAuthTokenErroredAction implements Action1<SyncEvent> {
        @Override
        public void call(SyncEvent syncEvent) {
            if (syncEvent.getState() == SyncState.ERROR) {
                Log.v(TAG, "Got a new error event from the login cache.");
                if (syncEvent.getError() != null) {
                    mPasswordView.setError(syncEvent.getError().getMessage());
                } else {
                    mPasswordView.setError("Login error, check your credentials.");
                }
                mPasswordView.requestFocus();
                showProgress(false);
                isLoggingIn = false;
            }
        }
    }

}

