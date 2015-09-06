package io.kazak.schedule.view;

import android.support.annotation.NonNull;

import io.kazak.model.Event;

public interface ScheduleEventView {

    void updateWith(@NonNull Event event);

}
