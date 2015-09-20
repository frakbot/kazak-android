package io.kazak.schedule;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;
import java.util.List;

import io.kazak.BuildConfig;
import io.kazak.KazakApplication;
import io.kazak.R;
import io.kazak.base.DeveloperError;
import io.kazak.model.Id;
import io.kazak.navigation.Navigator;
import io.kazak.repository.DataRepository;
import io.kazak.repository.event.SyncEvent;
import io.kazak.schedule.view.ScheduleEventView;
import io.kazak.schedule.view.table.ScheduleTableAdapter;
import io.kazak.schedule.view.table.ScheduleTableView;
import io.kazak.schedule.view.table.base.RulerView;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class ScheduleActivity extends AppCompatActivity implements ScheduleEventView.Listener, NavigationView.OnNavigationItemSelectedListener {

    private final CompositeSubscription subscriptions;

    @Inject
    DataRepository dataRepository;

    private Navigator navigator;

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
        navigator = new Navigator(this);

        setContentView(R.layout.activity_schedule);

        contentRootView = findViewById(R.id.content_root);
        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        navigationView = (NavigationView) findViewById(R.id.drawer_menu);
        toolbar = (Toolbar) findViewById(R.id.appbar);
        scheduleView = (ScheduleTableView) findViewById(R.id.schedule);

        setupScheduleView();
        setupAppBar();
        setupNavigationDrawer();

        subscribeToSchedule();
    }

    private void setupScheduleView() {
        scheduleView.setListener(this);
        scheduleView.setRoomsRuler((RulerView) findViewById(R.id.rooms_ruler));
        scheduleView.setTimeRuler((RulerView) findViewById(R.id.time_ruler));
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

    private void setupNavigationDrawer() {
        if (BuildConfig.DEBUG) {
            navigationView.inflateMenu(R.menu.drawer_debug);
        }
        navigationView.setCheckedItem(R.id.menu_nav_schedule);
        navigationView.setNavigationItemSelectedListener(this);
        hackToHideNavDrawerHeaderRipple();
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
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.clear();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // TODO use navigator here
        switch (item.getItemId()) {
            case R.id.menu_nav_schedule:
                // Do nothing: we're already there
                break;
            case R.id.menu_nav_get_to_the_venue:
                navigator.toArrivalInfo();
                break;
            case R.id.menu_nav_floor_plan:
                navigator.toVenueMap();
                break;
            case R.id.menu_nav_settings:
                navigator.toSettings();
                break;
            case R.id.menu_nav_debug:
                navigator.toDebug();
                break;
            default:
                throw new DeveloperError("Menu item " + item + " not supported");
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateWith(@NonNull ScheduleTableAdapter.Data data) {
        scheduleView.updateWith(data);
    }

    private void updateWith(@NonNull List<? extends Id> favorites) {
        scheduleView.updateWith(favorites);
    }

    @Override
    public void onTalkClicked(Id talkId) {
        navigator.toSessionDetails(talkId);
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleSyncError(SyncEvent syncEvent) {
        Throwable error = syncEvent.getError();
        if (error instanceof DeveloperError) {
            showSyncDeveloperError(error);
        } else {
            showSyncError();
        }
    }

    private void showSyncDeveloperError(Throwable error) {
        String message = getString(R.string.developer_error_loading_schedule, error.getMessage());
        Snackbar.make(contentRootView, message, Snackbar.LENGTH_INDEFINITE)
                .show();
    }

    private void showSyncError() {
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
                    handleSyncError(syncEvent);
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
