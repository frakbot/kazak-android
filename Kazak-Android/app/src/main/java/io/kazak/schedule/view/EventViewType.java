package io.kazak.schedule.view;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.kazak.R;
import io.kazak.base.DeveloperError;
import io.kazak.model.Event;
import io.kazak.model.EventType;
import io.kazak.model.Id;
import io.kazak.model.Room;
import io.kazak.model.Speaker;
import io.kazak.model.Speakers;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import rx.functions.Func1;

public enum EventViewType {

    TALK(R.layout.view_schedule_talk_card, false, createMaxHeightTalk())/*,
    //TODO add layouts for all event types
    CEREMONY(...),
    COFFEE_BREAK(...),
    PLACEHOLDER(...)*/;

    private static final Id NO_ID = new Id("");

    @NonNull
    public static EventViewType valueOf(@NonNull Event event) {
        return valueOf(event.type());
    }

    @NonNull
    public static EventViewType valueOf(@NonNull EventType eventType) {
        String name = eventType.name();
        try {
            return EventViewType.valueOf(name);
        }
        catch (IllegalArgumentException e) {
            throw new DeveloperError(e, "No %s for %s with name %s.", EventViewType.class.getSimpleName(), EventType.class.getSimpleName(), name);
        }
    }

    private final int layoutRes;
    private final boolean isPlaceholder;
    private final Func1<Integer, ? extends Event> maxHeightReference;

    EventViewType(
            @LayoutRes int layoutRes,
            boolean isPlaceholder,
            @NonNull Func1<Integer, ? extends Event> maxHeightReference) {
        this.layoutRes = layoutRes;
        this.isPlaceholder = isPlaceholder;
        this.maxHeightReference = maxHeightReference;
    }

    @LayoutRes
    public int getLayoutRes() {
        return layoutRes;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    @NonNull
    public Func1<Integer, ? extends Event> getMaxHeightReference() {
        return maxHeightReference;
    }

    @NonNull
    private static Func1<Integer, Talk> createMaxHeightTalk() {
        return new Func1<Integer, Talk>() {
            @Override
            public Talk call(@NonNull Integer timeSlotDurationMilliseconds) {
                Date start = new Date();
                Date end = new Date(start.getTime() + timeSlotDurationMilliseconds);
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

    @NonNull
    private static Speaker createDummySpeaker(String speakerName) {
        return new Speaker(NO_ID, speakerName, null, null, null, null);
    }

}
