package io.kazak.schedule.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;

import io.kazak.model.Schedule;
import io.kazak.model.Talk;

public class ScheduleView extends ListView {

    public ScheduleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScheduleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScheduleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void updateWith(@NonNull Schedule schedule) {
        List<Talk> talks = getTalksFrom(schedule);
        ScheduleAdapter adapter = new ScheduleAdapter(getContext(), talks);
        setAdapter(adapter);
    }

    private static List<Talk> getTalksFrom(Schedule schedule) {
        if (schedule.getDays().isEmpty()) {
            return Collections.emptyList();
        } else {
            return schedule.getDays().get(0).getTalks();
        }
    }

}
