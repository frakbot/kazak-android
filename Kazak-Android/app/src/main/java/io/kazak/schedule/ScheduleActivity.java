package io.kazak.schedule;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

import javax.inject.Inject;
import java.util.List;

import io.kazak.KazakApplication;
import io.kazak.KazakNavDrawerActivity;
import io.kazak.R;
import io.kazak.base.DeveloperError;
import io.kazak.model.Id;
import io.kazak.repository.DataRepository;
import io.kazak.repository.event.SyncEvent;
import io.kazak.schedule.view.ScheduleEventView;
import io.kazak.schedule.view.table.ScheduleTableAdapter;
import io.kazak.schedule.view.table.ScheduleTableView;
import io.kazak.schedule.view.table.base.RulerView;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class ScheduleActivity extends KazakNavDrawerActivity implements ScheduleEventView.Listener {

    private final CompositeSubscription subscriptions;

    @Inject
    DataRepository dataRepository;

    private View contentRootView;
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
        scheduleView = (ScheduleTableView) findViewById(R.id.schedule);

        setupScheduleView();
        setupAppBar();

        subscribeToSchedule();
    }

    private void setupScheduleView() {
        scheduleView.setListener(this);
        scheduleView.setRoomsRuler((RulerView) findViewById(R.id.rooms_ruler));
        scheduleView.setTimeRuler((RulerView) findViewById(R.id.time_ruler));
    }

    private void setupAppBar() {
        getSupportAppBar().setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openNavigationDrawer();
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

    private void updateWith(@NonNull ScheduleTableAdapter.Data data) {
        scheduleView.updateWith(data);
    }

    private void updateWith(@NonNull List<? extends Id> favorites) {
        scheduleView.updateWith(favorites);
    }

    @Override
    public void onTalkClicked(Id talkId) {
        navigate().toSessionDetails(talkId);
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

    @Override
    protected int getNavigationDrawerMenuIdForThisActivity() {
        return R.id.menu_nav_schedule;
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
