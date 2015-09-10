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
import android.support.design.widget.Snackbar;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.inject.Inject;
import java.util.List;

import io.kazak.KazakApplication;
import io.kazak.R;
import io.kazak.auth.KazakAuthToken;
import io.kazak.repository.AuthRepository;
import io.kazak.repository.event.SyncEvent;
import io.kazak.repository.event.SyncState;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class LoginActivity extends Activity {

    // unused for now, can be used to discriminate whether this is a new login or a confirmation for an existing one
    public static final String NEW_ACCOUNT = "NEW_ACCOUNT";
    // the token type, specifies the kind of login to perform (credentials, google, facebook, twitter, ..)
    public static final String AUTH_TOKEN_TYPE = "AUTH_TOKEN_TYPE";
    // default token type (username and password)
    private static final String AUTH_TOKEN_TYPE_PASSWORD = "AUTH_TOKEN_TYPE_PASSWORD";
    // bundle state key, if its value is true the login cache will be invalidated when the activity is created
    private static final String BUNDLE_NEED_NEW_CACHE = "BUNDLE_NEW_CACHE";

    @Inject
    AuthRepository authRepository;

    private CompositeSubscription loginSubscriptions;

    // UI references
    private LinearLayout contentRootView;
    private AutoCompleteTextView emailView;
    private EditText passwordView;
    private View progressView;
    private View loginFormView;

    public LoginActivity() {
        loginSubscriptions = new CompositeSubscription();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KazakApplication.injector().inject(this);

        setContentView(R.layout.activity_login);

        contentRootView = (LinearLayout) findViewById(R.id.login_root_layout);

        // if this is a new login session, clear the login cache
        if (savedInstanceState == null || !savedInstanceState.getBoolean(BUNDLE_NEED_NEW_CACHE)) {
            authRepository.clearLoginCache();
        }

        // Set up the login form.
        emailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        passwordView = (EditText) findViewById(R.id.password);
        // Handle enter on the password
        passwordView.setOnEditorActionListener(
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

        Button signIn = (Button) findViewById(R.id.email_sign_in_button);
        signIn.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(@NonNull View view) {
                        attemptLogin();
                    }
                }
        );

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        List<String> emails = LoginPackage.getDeviceUserEmails(this);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emails
                );
        emailView.setAdapter(adapter);
    }

    private void attemptLogin() {
        unsubscribe();
        authRepository.clearLoginCache();
        subscribe();

        // Store values at the time of the login attempt
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        View erroredView = validateLoginData(email, password);

        if (erroredView != null) {
            // There was an error; don't attempt login and focus the first form field with an error
            erroredView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to perform the user login attempt
            hideKeyboard();
            authRepository.login(email, password);
        }
    }

    private View validateLoginData(String email, String password) {
        // Reset errors
        emailView.setError(null);
        passwordView.setError(null);

        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            showErrorSnackbar(getString(R.string.error_invalid_password));
            focusView = passwordView;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            showErrorSnackbar(getString(R.string.error_email_required));
            focusView = emailView;
        } else if (!LoginPackage.isEmailValid(email)) {
            showErrorSnackbar(getString(R.string.error_invalid_email));
            focusView = emailView;
        }

        return focusView;
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        subscribe();
    }

    private void subscribe() {
        // Subscribe to the cache and sync state cache
        loginSubscriptions.add(authRepository.getLastLoginSyncEvents().subscribe(new OnAuthTokenErroredAction(this)));
        loginSubscriptions.add(authRepository.getLoginCache().subscribe(new OnAuthTokenRetrievedAction(this)));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unsubscribe();
    }

    private void unsubscribe() {
        // unsubscribe from all the things and re-instantiate the composite (needed to avoid losing notifications)
        loginSubscriptions.unsubscribe();
        loginSubscriptions = new CompositeSubscription();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(BUNDLE_NEED_NEW_CACHE, true);
        super.onSaveInstanceState(outState);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1
            ).setListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(@NonNull Animator animation) {
                            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    }
            );

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0
            ).setListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(@NonNull Animator animation) {
                            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    }
            );
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showErrorSnackbar(String errorMessage) {
        Snackbar errorSnackbar = Snackbar.make(contentRootView, errorMessage, Snackbar.LENGTH_LONG);
        // when the error snackbar is dismissed for any reason, clean the error state so it doesn't get shown anymore
        errorSnackbar.setCallback(
                new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        authRepository.forceIdleState();
                    }
                }
        );
        // show the error snackbar
        errorSnackbar.show();
    }

    private static class OnAuthTokenRetrievedAction implements Action1<KazakAuthToken> {
        private final LoginActivity activity;

        public OnAuthTokenRetrievedAction(LoginActivity activity) {
            this.activity = activity;
        }

        @Override
        public void call(KazakAuthToken authToken) {
            Intent originalIntent = activity.getIntent();
            String accountName = activity.emailView.getText().toString();
            final Account account = new Account(accountName, originalIntent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
            String authTokenType = originalIntent.getStringExtra(AUTH_TOKEN_TYPE);
            if (authTokenType == null) {
                authTokenType = AUTH_TOKEN_TYPE_PASSWORD;
            }
            // Explicitly add an account with null password
            final AccountManager mAccountManager = AccountManager.get(activity);
            mAccountManager.addAccountExplicitly(account, null, null);
            // Save the token credentials in the AccountManager
            mAccountManager.setAuthToken(account, authTokenType, authToken.getToken());
            activity.setResult(RESULT_OK, originalIntent);
            activity.finish();
        }
    }

    private static class OnAuthTokenErroredAction implements Action1<SyncEvent> {
        private final LoginActivity activity;

        public OnAuthTokenErroredAction(LoginActivity activity) {
            this.activity = activity;
        }

        // we don't care about throwing the error out of this method since we're handling it here
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        @Override
        public void call(SyncEvent syncEvent) {
            // show the progress iif the last state is loading, otherwise hide it
            activity.showProgress(syncEvent.getState() == SyncState.LOADING);
            // if we have an error, show a message
            if (syncEvent.getState() == SyncState.ERROR) {
                Log.e("Kazak", "Failed to log user in", syncEvent.getError());
                String errorMessage = activity.getString(R.string.error_login);
                if (syncEvent.getError() != null && syncEvent.getError().getMessage() != null) {
                    errorMessage = syncEvent.getError().getMessage();
                }
                activity.showErrorSnackbar(errorMessage);
                activity.passwordView.requestFocus();
            }
        }
    }

}

