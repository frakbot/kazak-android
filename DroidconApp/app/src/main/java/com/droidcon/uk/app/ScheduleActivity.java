package com.droidcon.uk.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ScheduleActivity extends AppCompatActivity {

    private final CompositeSubscription subscriptions = new CompositeSubscription();

    DataProvider dataProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscriptions.add(
                dataProvider.getSchedule()
                        .subscribeOn(Schedulers.io())
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
