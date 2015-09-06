package io.kazak.schedule.view.table;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.kazak.R;
import io.kazak.model.Room;
import io.kazak.model.Schedule;
import io.kazak.model.Speaker;
import io.kazak.model.Speakers;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import io.kazak.schedule.view.TalkView;
import io.kazak.schedule.view.table.base.TableItemPaddingDecoration;
import io.kazak.schedule.view.table.base.TableLayoutManager;

public class ScheduleTableView extends RecyclerView {

    private static final int UNSPECIFIED_MEASURE_SPEC = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    private static final String NO_ID = "";

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
        int timeSlotUnitWidthPx = a.getDimensionPixelSize(R.styleable.ScheduleTableView_timeSlotUnitWidth, 0);
        int timeSlotDurationMinutes = a.getInt(R.styleable.ScheduleTableView_timeSlotUnitDurationMinutes, 0);
        a.recycle();

        adapter = new ScheduleTableAdapter(context);

        int timeSlotDurationMilliseconds = (int) TimeUnit.MINUTES.toMillis(timeSlotDurationMinutes);
        int rowHeightPx = computeRowHeight(timeSlotUnitWidthPx, timeSlotDurationMinutes);

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
            return schedule.getDays().get(0).getTalks(); //TODO
        }
    }

    private int computeRowHeight(int timeSlotUnitWidthPx, int timeSlotDurationMinutes) {
        ScheduleTableViewHolder viewHolder = adapter.createMaxHeightReferenceViewHolder();
        TalkView talkView = viewHolder.getTalkView();

        talkView.updateWith(createMaxHeightReferenceTalk(timeSlotDurationMinutes));

        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(timeSlotUnitWidthPx, MeasureSpec.EXACTLY);
        talkView.measure(widthMeasureSpec, UNSPECIFIED_MEASURE_SPEC);

        return talkView.getMeasuredHeight();
    }

    @NonNull
    private Talk createMaxHeightReferenceTalk(int timeSlotDurationMinutes) {
        Date start = new Date();
        Date end = new Date(start.getTime() + TimeUnit.MINUTES.toMillis(timeSlotDurationMinutes));
        List<Speaker> speakerList = Arrays.asList(createDummySpeaker("Speaker1"), createDummySpeaker("Speaker2"));
        return new Talk(
                NO_ID,
                "1\n2\n3",
                new TimeSlot(start, end),
                Collections.singletonList(new Room(NO_ID, "Room")),
                new Speakers(speakerList),
                null);
    }

    @NonNull
    private static Speaker createDummySpeaker(String speakerName) {
        return new Speaker(NO_ID, speakerName, null, null, null, null);
    }

}
