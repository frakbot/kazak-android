package io.kazak.schedule;

import android.support.annotation.NonNull;

import io.kazak.model.Schedule;
import io.kazak.schedule.view.table.ScheduleTableAdapter;
import io.kazak.schedule.view.table.ScheduleTableView;
import rx.functions.Func1;

final class ScheduleActivityFunctions {

    @NonNull
    public static Func1<Schedule, ScheduleTableAdapter.Data> createAdapterData(@NonNull final ScheduleTableView scheduleView) {
        return new Func1<Schedule, ScheduleTableAdapter.Data>() {
            @Override
            public ScheduleTableAdapter.Data call(Schedule schedule) {
                return scheduleView.createAdapterData(schedule);
            }
        };
    }

    private ScheduleActivityFunctions() {
    }

}
