package com.droidcon.uk.app;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.droidcon.uk.app.model.Schedule;

import javax.inject.Inject;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class ScheduleActivity extends AppCompatActivity {

    private final CompositeSubscription subscriptions;

    @Inject
    DataRepository dataRepository;

    private FrameLayout contentRootView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    public ScheduleActivity() {
        subscriptions = new CompositeSubscription();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DroidconApplication.injector().inject(this);
        setContentView(R.layout.activity_schedule);
        contentRootView = (FrameLayout) findViewById(R.id.content_root);
        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        navigationView = (NavigationView) findViewById(R.id.drawer_menu);
        toolbar = (Toolbar) findViewById(R.id.appbar);

        setupAppBar();
        hackToHideNavDrawerHeaderRipple();
    }

    private void hackToHideNavDrawerHeaderRipple() {
        // TODO remove this when 22.2.1 is released
        // See https://code.google.com/p/android/issues/detail?id=176400
        View navigationHeader = findViewById(R.id.navigation_header);
        ((View) navigationHeader.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do nothing
            }
        });
    }

    private void setupAppBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscribeToSchedule();
    }

    private void subscribeToSchedule() {
        subscriptions.add(
                dataRepository.getSchedule()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new ScheduleObserver())
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscriptions.clear();
    }

    private void updateWith(Schedule schedule) {
        // TODO
    }

    private class ScheduleObserver implements Observer<Schedule> {

        @Override
        public void onCompleted() {
            // No-op
        }

        @Override
        public void onError(Throwable e) {
            Snackbar.make(contentRootView, R.string.error_loading_schedule, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            subscribeToSchedule();
                        }
                    })
                    .show();
        }

        @Override
        public void onNext(Schedule schedule) {
            updateWith(schedule);
        }
    }

}
