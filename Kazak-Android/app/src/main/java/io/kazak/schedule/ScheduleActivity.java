package io.kazak.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import javax.inject.Inject;
import java.util.List;

import io.kazak.KazakApplication;
import io.kazak.R;
import io.kazak.base.DeveloperError;
import io.kazak.model.Id;
import io.kazak.repository.DataRepository;
import io.kazak.repository.event.SyncEvent;
import io.kazak.schedule.view.ScheduleEventView;
import io.kazak.schedule.view.table.ScheduleTableAdapter;
import io.kazak.schedule.view.table.ScheduleTableView;
import io.kazak.schedule.view.table.base.RulerView;
import io.kazak.talk.TalkDetailsActivity;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class ScheduleActivity extends AppCompatActivity implements ScheduleEventView.Listener {

    private final CompositeSubscription subscriptions;

    @Inject
    DataRepository dataRepository;

    private View contentRootView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ScheduleTableView scheduleView;

    public ScheduleActivity() {
        subscriptions = new CompositeSubscription();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KazakApplication.injector().inject(this);
        setContentView(R.layout.activity_schedule);

        contentRootView = findViewById(R.id.content_root);
        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        navigationView = (NavigationView) findViewById(R.id.drawer_menu);
        toolbar = (Toolbar) findViewById(R.id.appbar);
        scheduleView = (ScheduleTableView) findViewById(R.id.schedule);

        setupScheduleView();
        setupAppBar();
        hackToHideNavDrawerHeaderRipple();
    }

    private void setupScheduleView() {
        scheduleView.setListener(this);
        scheduleView.setRoomsRuler((RulerView) findViewById(R.id.rooms_ruler));
        scheduleView.setTimeRuler((RulerView) findViewById(R.id.time_ruler));
    }

    private void hackToHideNavDrawerHeaderRipple() {
        // TODO remove this when the issue is fixed
        // See https://code.google.com/p/android/issues/detail?id=176400
        View navigationHeader = findViewById(R.id.navigation_header);
        ((View) navigationHeader.getParent()).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Do nothing
                    }
                }
        );
    }

    private void setupAppBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawerLayout.openDrawer(navigationView);
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscribeToSchedule();
    }

    private void subscribeToSchedule() {
        subscriptions.add(
                dataRepository.getSchedule()
                        .map(ScheduleActivityFunctions.createAdapterData(scheduleView))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new ScheduleObserver())
        );
        subscriptions.add(
                dataRepository.getFavoriteIds()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new FavoritesObserver())
        );
        subscriptions.add(
                dataRepository.getScheduleSyncEvents()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SyncEventObserver())
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscriptions.clear();
    }

    private void updateWith(@NonNull ScheduleTableAdapter.Data data) {
        scheduleView.updateWith(data);
    }

    private void updateWith(@NonNull List<? extends Id> favorites) {
        scheduleView.updateWith(favorites);
    }

    @Override
    public void onTalkClicked(Id talkId) {
        Intent intent = new Intent(this, TalkDetailsActivity.class);
        intent.putExtra(TalkDetailsActivity.EXTRA_TALK_ID, talkId.getId());
        startActivity(intent);
    }

    private class ScheduleObserver implements Observer<ScheduleTableAdapter.Data> {

        @Override
        public void onCompleted() {
            // No-op
        }

        @Override
        public void onError(Throwable e) {
            throw new IllegalStateException(e);
        }

        @Override
        public void onNext(ScheduleTableAdapter.Data data) {
            updateWith(data);
        }

    }

    private class FavoritesObserver implements Observer<List<? extends Id>> {

        @Override
        public void onCompleted() {
            // No-op
        }

        @Override
        public void onError(Throwable e) {
            throw new IllegalStateException(e);
        }

        @Override
        public void onNext(List<? extends Id> favorites) {
            updateWith(favorites);
        }

    }

    private class SyncEventObserver implements Observer<SyncEvent> {

        @Override
        public void onCompleted() {
            // Ignore
        }

        @Override
        public void onError(Throwable e) {
            throw new IllegalStateException(e);
        }

        @Override
        public void onNext(SyncEvent syncEvent) {
            switch (syncEvent.getState()) {
                case ERROR:
                    Log.e("Kazak", "Failed to load schedule", syncEvent.getError());
                    Snackbar.make(contentRootView, R.string.error_loading_schedule, Snackbar.LENGTH_LONG)
                            .setAction(
                                    R.string.action_retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            subscribeToSchedule();
                                        }
                                    }
                            )
                            .show();
                    break;
                case IDLE:
                    //Display empty screen if no data
                    break;
                case LOADING:
                    //Display loading screen
                    break;
                default:
                    throw new DeveloperError("Sync event '" + syncEvent.getState() + "' is not supported");
            }
        }
    }

}
