package com.droidcon.uk.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.droidcon.uk.app.model.Schedule;

import javax.inject.Inject;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class ScheduleActivity extends AppCompatActivity {

    private final CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    DataRepository dataRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DroidconApplication.injector().inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    }

    private class ScheduleObserver implements Observer<Schedule> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Schedule schedule) {
            updateWith(schedule);
        }
    }
}
