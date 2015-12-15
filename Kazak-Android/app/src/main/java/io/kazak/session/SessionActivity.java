/**
 * Copyright (c) 2015, Facebook, Inc.  All rights reserved.
 * <p/>
 * Facebook, Inc. (“Facebook”) owns all right, title and interest, including all intellectual
 * property and other proprietary rights, in and to its contributions to the Droidcon App software
 * (the “Contributions”). Subject to your compliance with these terms, you are hereby granted a
 * non-exclusive, worldwide, royalty-free copyright license to (1) use and copy the Contributions;
 * (2) reproduce and distribute the Contributions as part of your own software (“Your Software”),
 * provided Your Software does not consist solely of the Contributions; and
 * (3) modify the Contributions for your own internal use.
 * Facebook reserves all rights not expressly granted to you in this license agreement.
 * <p/>
 * THE CONTRIBUTIONS AND DOCUMENTATION, IF ANY, ARE PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL FACEBOOK OR ITS AFFILIATES, OFFICERS,
 * DIRECTORS OR EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THE CONTRIBUTIONS, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.kazak.session;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;
import java.util.List;

import io.kazak.BuildConfig;
import io.kazak.KazakActivity;
import io.kazak.KazakApplication;
import io.kazak.R;
import io.kazak.model.Event;
import io.kazak.model.Id;
import io.kazak.model.Session;
import io.kazak.repository.DataRepository;
import io.kazak.repository.event.SyncEvent;
import io.kazak.session.view.SessionContentView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class SessionActivity extends KazakActivity {

    public static final String EXTRA_SESSION_ID = BuildConfig.APPLICATION_ID + ".extra.SESSION_ID";

    private final CompositeSubscription subscriptions;

    @Inject
    DataRepository dataRepository;

    private Id sessionId;
    private TextView titleView;
    private SessionContentView sessionContentView;
    private FloatingActionButton favouriteFab;

    public SessionActivity() {
        subscriptions = new CompositeSubscription();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KazakApplication.injector().inject(this);
        setContentView(R.layout.activity_session);

        titleView = (TextView) findViewById(R.id.session_title);
        sessionContentView = (SessionContentView) findViewById(R.id.session_content);
        favouriteFab = (FloatingActionButton) findViewById(R.id.session_favorite_fab);

        sessionId = extractSessionIdFrom(getIntent());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        subscribeToSession(sessionId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        subscriptions.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.session_map:
                navigate().toVenueMap();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateWith(Session session) {
        titleView.setText(session.name());
        sessionContentView.updateWith(session);
    }

    private void showFavoriteIconOn() {
        favouriteFab.setImageResource(R.drawable.ic_star_filled_20dp);
    }

    private void showNotFavoriteIconOn() {
        // TODO: the current empty is gray instead of white, not very visible
        favouriteFab.setImageResource(R.drawable.ic_star_empty_20dp);
    }

    private void subscribeToSession(Id sessionId) {
        subscriptions.add(
                dataRepository.getEvent(sessionId)
                        .map(asSession())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SessionObserver())
        );
        subscriptions.add(
                dataRepository.getScheduleSyncEvents()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SyncEventObserver())
        );
        subscriptions.add(
                dataRepository.getFavoriteIds()
                        .map(asFavoriteBoolean(sessionId))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new FavouriteObserver())
        );

    }

    private void showErrorSnackbar() {
        Snackbar.make(sessionContentView, R.string.error_loading_schedule, Snackbar.LENGTH_LONG)
                .setAction(
                        R.string.action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                subscribeToSession(sessionId);
                            }
                        })
                .show();
    }

    private static Id extractSessionIdFrom(Intent intent) {
        return new Id(intent.getStringExtra(EXTRA_SESSION_ID));
    }

    private static Func1<Event, Session> asSession() {
        return new Func1<Event, Session>() {
            @Override
            public Session call(Event event) {
                return (Session) event;
            }
        };
    }

    private Func1<List<? extends Id>, Boolean> asFavoriteBoolean(final Id sessionId) {
        return new Func1<List<? extends Id>, Boolean>() {
            @Override
            public Boolean call(List<? extends Id> ids) {
                for (Id id : ids) {
                    if (id.getId().equals(sessionId.getId())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private class SessionObserver extends SimpleObserver<Session> {

        @Override
        public void onNext(Session session) {
            updateWith(session);
        }
    }

    private class SyncEventObserver extends SimpleObserver<SyncEvent> {

        @Override
        public void onNext(SyncEvent syncEvent) {
            switch (syncEvent.getState()) {
                case ERROR:
                    showErrorSnackbar();
                    break;

                case IDLE:
                    //Display empty screen if no data
                    break;

                case LOADING:
                    //Display loading screen
                    break;
            }
        }
    }

    private class FavouriteObserver extends SimpleObserver<Boolean> {

        @Override
        public void onNext(Boolean isFavorite) {
            if (isFavorite) {
                showFavoriteIconOn();
            } else {
                showNotFavoriteIconOn();
            }
        }

    }

}
