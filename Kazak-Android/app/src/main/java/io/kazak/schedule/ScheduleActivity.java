package io.kazak.schedule;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import javax.inject.Inject;

import io.kazak.KazakApplication;
import io.kazak.R;
import io.kazak.base.DeveloperError;
import io.kazak.repository.DataRepository;
import io.kazak.repository.event.SyncEvent;
import io.kazak.schedule.view.table.ScheduleTableAdapter;
import io.kazak.schedule.view.table.ScheduleTableView;
import io.kazak.schedule.view.table.base.Ruler;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class ScheduleActivity extends AppCompatActivity {

    private final CompositeSubscription subscriptions;

    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
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

        setupRulers();
        setupAppBar();
        hackToHideNavDrawerHeaderRipple();
    }

    private void setupRulers() {
        ((Ruler) findViewById(R.id.rooms_ruler)).bind(scheduleView);
        ((Ruler) findViewById(R.id.time_ruler)).bind(scheduleView);
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
