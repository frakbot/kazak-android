package io.kazak.schedule.view.table;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.Collections;
import java.util.List;

import io.kazak.R;
import io.kazak.model.Schedule;
import io.kazak.model.Talk;
import io.kazak.schedule.view.table.base.TableItemPaddingDecoration;
import io.kazak.schedule.view.table.base.TableLayoutManager;

public class ScheduleTableView extends RecyclerView {

    private final ScheduleTableAdapter adapter;

    public ScheduleTableView(Context context) {
        this(context, null);
    }

    public ScheduleTableView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.scheduleViewDefaultStyle);
    }

    public ScheduleTableView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.ScheduleViewDefaultStyle);
    }

    public ScheduleTableView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        // this is the right super call as RecyclerView doesn't supported the 4th View constructor
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScheduleTableView, defStyleAttr, defStyleRes);
        int itemsPaddingHorizontal = a.getDimensionPixelSize(R.styleable.ScheduleTableView_itemsPaddingHorizontal, 0);
        int itemsPaddingVertical = a.getDimensionPixelSize(R.styleable.ScheduleTableView_itemsPaddingVertical, 0);
        int rowHeightPx = a.getDimensionPixelSize(R.styleable.ScheduleTableView_rowHeight, 0);
        int timeSlotUnitWidthPx = a.getDimensionPixelSize(R.styleable.ScheduleTableView_timeSlotUnitWidth, 0);
        int timeSlotDurationMinutes = a.getInt(R.styleable.ScheduleTableView_timeSlotUnitDurationMinutes, 0);
        a.recycle();

        int timeSlotDurationMilliseconds = timeSlotDurationMinutes * 60 * 1000;

        adapter = new ScheduleTableAdapter(context);
        setHasFixedSize(true);
        addItemDecoration(new TableItemPaddingDecoration(itemsPaddingHorizontal, itemsPaddingVertical));
        setLayoutManager(
                new TableLayoutManager(
                        rowHeightPx, timeSlotUnitWidthPx, timeSlotDurationMilliseconds, itemsPaddingHorizontal, itemsPaddingVertical));
        setAdapter(adapter);
    }

    public void updateWith(@NonNull ScheduleTableAdapter.Data data) {
        adapter.updateWith(data);
    }

    @NonNull
    public ScheduleTableAdapter.Data createAdapterData(@NonNull Schedule schedule) {
        return createAdapterData(getTalksFrom(schedule));
    }

    @NonNull
    public ScheduleTableAdapter.Data createAdapterData(@NonNull List<Talk> talks) {
        return adapter.createSortedData(talks);
    }

    @NonNull
    private static List<Talk> getTalksFrom(@NonNull Schedule schedule) {
        if (schedule.getDays().isEmpty()) {
            return Collections.emptyList();
        } else {
            return schedule.getDays().get(0).getTalks();
        }
    }

}
