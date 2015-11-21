package io.kazak.schedule.view.table;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.kazak.R;
import io.kazak.model.Event;
import io.kazak.model.EventType;
import io.kazak.model.Id;
import io.kazak.model.Schedule;
import io.kazak.schedule.view.EventViewType;
import io.kazak.schedule.view.ScheduleEventView;
import io.kazak.schedule.view.table.base.Ruler;
import io.kazak.schedule.view.table.base.TableItemPaddingDecoration;
import io.kazak.schedule.view.table.base.TableLayoutManager;

public class ScheduleTableView extends RecyclerView {

    private static final int UNSPECIFIED_MEASURE_SPEC = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

    private final ScheduleTableAdapter adapter;
    private final TableLayoutManager layoutManager;

    public ScheduleTableView(Context context) {
        this(context, null);
    }

    public ScheduleTableView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.scheduleTableViewDefaultStyle);
    }

    public ScheduleTableView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.ScheduleTableViewDefaultStyle);
    }

    public ScheduleTableView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        // this is the right super call as RecyclerView doesn't supported the 4th View constructor
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScheduleTableView, defStyleAttr, defStyleRes);
        int itemsPaddingHorizontal = a.getDimensionPixelSize(R.styleable.ScheduleTableView_itemsPaddingHorizontal, 0);
        int itemsPaddingVertical = a.getDimensionPixelSize(R.styleable.ScheduleTableView_itemsPaddingVertical, 0);
        int timeSlotUnitWidthPx = a.getDimensionPixelSize(R.styleable.ScheduleTableView_timeSlotUnitWidth, 0);
        int timeSlotDurationMinutes = a.getInt(R.styleable.ScheduleTableView_timeSlotUnitDurationMinutes, 0);
        a.recycle();

        adapter = new ScheduleTableAdapter(context);

        int timeSlotDurationMilliseconds = (int) TimeUnit.MINUTES.toMillis(timeSlotDurationMinutes);
        int rowHeightPx = computeRowHeight(timeSlotUnitWidthPx, timeSlotDurationMilliseconds);
        layoutManager = new TableLayoutManager(
                rowHeightPx, timeSlotUnitWidthPx, timeSlotDurationMilliseconds, itemsPaddingHorizontal, itemsPaddingVertical);

        setHasFixedSize(true);
        addItemDecoration(new TableItemPaddingDecoration(itemsPaddingHorizontal, itemsPaddingVertical));
        setLayoutManager(layoutManager);
        setAdapter(adapter);
    }

    public void setListener(@Nullable ScheduleEventView.Listener listener) {
        adapter.setListener(listener);
    }

    public void updateWith(@NonNull ScheduleTableAdapter.Data data) {
        adapter.updateWith(data);
    }

    public void updateWith(@NonNull List<? extends Id> favorites) {
        adapter.updateWith(favorites);
    }

    @NonNull
    public ScheduleTableAdapter.Data createAdapterData(@NonNull Schedule schedule) {
        return createAdapterData(getEventsFrom(schedule));
    }

    @NonNull
    public ScheduleTableAdapter.Data createAdapterData(@NonNull List<? extends Event> events) {
        return adapter.createSortedData(events);
    }

    @NonNull
    private static List<? extends Event> getEventsFrom(@NonNull Schedule schedule) {
        if (schedule.getDays().isEmpty()) {
            return Collections.emptyList();
        } else {
            final List<Event> events = schedule.getDays().get(0).getEvents();
            List<Event> supportedEvents = new ArrayList<>();
            for (Event event : events) {
                if (event.type() == EventType.TALK || event.type() == EventType.COFFEE_BREAK || event.type() == EventType.CEREMONY) {
                    supportedEvents.add(event);
                }
            }
            return supportedEvents; //TODO
        }
    }

    private int computeRowHeight(int timeSlotUnitWidthPx, int timeSlotDurationMilliseconds) {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(timeSlotUnitWidthPx, MeasureSpec.EXACTLY);
        int rowHeight = 0;
        for (EventViewType eventViewType : EventViewType.values()) {
            View view = adapter.getMaxHeightReferenceView(eventViewType, timeSlotDurationMilliseconds);
            view.measure(widthMeasureSpec, UNSPECIFIED_MEASURE_SPEC);
            rowHeight = Math.max(rowHeight, view.getMeasuredHeight());
        }
        return rowHeight;
    }

    public void setRoomsRuler(@Nullable Ruler ruler) {
        layoutManager.setRowsRuler(ruler);
    }

    public void setTimeRuler(@Nullable Ruler ruler) {
        layoutManager.setBoundsRuler(ruler);
    }

}
