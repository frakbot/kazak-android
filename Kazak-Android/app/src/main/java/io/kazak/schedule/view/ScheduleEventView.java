package io.kazak.schedule.view;

import android.support.annotation.NonNull;

import io.kazak.model.Event;

public interface ScheduleEventView<T extends Event> {

    void updateWith(@NonNull T event);

}
