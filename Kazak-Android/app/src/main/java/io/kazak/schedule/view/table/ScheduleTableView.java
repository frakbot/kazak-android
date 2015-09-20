package io.kazak.schedule.view.table;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.kazak.R;
import io.kazak.model.Event;
import io.kazak.model.Id;
import io.kazak.model.Room;
import io.kazak.model.Schedule;
import io.kazak.model.Speaker;
import io.kazak.model.Speakers;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import io.kazak.schedule.view.ScheduleEventView;
import io.kazak.schedule.view.table.ScheduleTableAdapter.Layout;
import io.kazak.schedule.view.table.base.Ruler;
import io.kazak.schedule.view.table.base.TableItemPaddingDecoration;
import io.kazak.schedule.view.table.base.TableLayoutManager;
import rx.functions.Func0;

public class ScheduleTableView extends RecyclerView {

    @LayoutRes
    private static final int LAYOUT_TALK = R.layout.view_schedule_talk_card;

    private static final int UNSPECIFIED_MEASURE_SPEC = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    private static final Id NO_ID = new Id("");

    private final ScheduleTableAdapter adapter;
    private final TableLayoutManager layoutManager;

    @NonNull
    private final List<Layout<?>> supportedLayouts;

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
        supportedLayouts = createSupportedLayoutsList(timeSlotDurationMilliseconds);

        int rowHeightPx = computeRowHeight(timeSlotUnitWidthPx);
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
        return adapter.createSortedData(events, supportedLayouts);
    }

    @NonNull
    private static List<? extends Event> getEventsFrom(@NonNull Schedule schedule) {
        if (schedule.getDays().isEmpty()) {
            return Collections.emptyList();
        } else {
            return schedule.getDays().get(0).getTalks(); //TODO
        }
    }

    private int computeRowHeight(int timeSlotUnitWidthPx) {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(timeSlotUnitWidthPx, MeasureSpec.EXACTLY);
        int rowHeight = 0;
        for (Layout<?> layout : supportedLayouts) {
            View view = adapter.getMaxHeightReferenceView(layout);
            view.measure(widthMeasureSpec, UNSPECIFIED_MEASURE_SPEC);
            rowHeight = Math.max(rowHeight, view.getMeasuredHeight());
        }
        return rowHeight;
    }

    @NonNull
    private static Speaker createDummySpeaker(String speakerName) {
        return new Speaker(NO_ID, speakerName, null, null, null, null);
    }

    public void setRoomsRuler(@Nullable Ruler ruler) {
        layoutManager.setRowsRuler(ruler);
    }

    public void setTimeRuler(@Nullable Ruler ruler) {
        layoutManager.setBoundsRuler(ruler);
    }

    @NonNull
    private List<Layout<?>> createSupportedLayoutsList(int timeSlotDurationMilliseconds) {
        List<Layout<?>> layouts = new ArrayList<>(1);

        layouts.add(new Layout<>(Talk.class, LAYOUT_TALK, false, createMaxHeightTalk(timeSlotDurationMilliseconds)));
        //TODO add layouts for all event types

        return layouts;
    }

    @NonNull
    private Func0<Talk> createMaxHeightTalk(final int timeSlotDurationMinutes) {
        return new Func0<Talk>() {
            @Override
            public Talk call() {
                Date start = new Date();
                Date end = new Date(start.getTime() + TimeUnit.MINUTES.toMillis(timeSlotDurationMinutes));
                List<Speaker> speakerList = Arrays.asList(createDummySpeaker("Speaker1"), createDummySpeaker("Speaker2"));
                return new Talk(
                        NO_ID,
                        "1\n2\n3",
                        "description",
                        new TimeSlot(start, end),
                        Collections.singletonList(new Room(NO_ID, "Room")),
                        new Speakers(speakerList),
                        null);
            }
        };
    }

}
