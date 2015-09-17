package io.kazak.schedule.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.kazak.model.Event;
import io.kazak.model.Id;

public interface ScheduleEventView<T extends Event> {

    void updateWith(@NonNull T event, @Nullable Listener listener);

    interface Listener {

        void onTalkClicked(Id talkId);

    }

}
