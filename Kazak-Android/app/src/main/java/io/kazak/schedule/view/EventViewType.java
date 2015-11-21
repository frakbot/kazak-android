package io.kazak.schedule.view;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.kazak.R;
import io.kazak.base.DeveloperError;
import io.kazak.model.Ceremony;
import io.kazak.model.CoffeeBreak;
import io.kazak.model.Event;
import io.kazak.model.EventType;
import io.kazak.model.Id;
import io.kazak.model.Placeholder;
import io.kazak.model.Room;
import io.kazak.model.Speaker;
import io.kazak.model.Speakers;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import rx.functions.Func1;

public enum EventViewType {

    TALK(R.layout.view_schedule_talk_card, false, createMaxHeightTalk()),
    COFFEE_BREAK(R.layout.view_schedule_coffee_break_card, false, createMaxHeightCoffeeBreak()),
    CEREMONY(R.layout.view_schedule_ceremony_card, false, createMaxHeightCeremony()),
    PLACEHOLDER(R.layout.view_schedule_placeholder_card, false, createMaxHeightPlaceholder());

    private static final Id NO_ID = new Id("");
    private static final String DUMMY_NAME = "1\n2\n3";
    private static final String DUMMY_DESCRIPTION = "description";

    @NonNull
    public static EventViewType valueOf(@NonNull Event event) {
        return valueOf(event.type());
    }

    @NonNull
    public static EventViewType valueOf(@NonNull EventType eventType) {
        String name = eventType.name();
        try {
            return EventViewType.valueOf(name);
        } catch (IllegalArgumentException e) {
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
                        DUMMY_NAME,
                        "description",
                        new TimeSlot(start, end),
                        Collections.singletonList(new Room(NO_ID, "Room")),
                        new Speakers(speakerList),
                        null);
            }
        };
    }

    @NonNull
    private static Func1<Integer, CoffeeBreak> createMaxHeightCoffeeBreak() {
        return new Func1<Integer, CoffeeBreak>() {
            @Override
            public CoffeeBreak call(@NonNull Integer timeSlotDurationMilliseconds) {
                Date start = new Date();
                Date end = new Date(start.getTime() + timeSlotDurationMilliseconds);
                return new CoffeeBreak(NO_ID, DUMMY_NAME, DUMMY_DESCRIPTION, new TimeSlot(start, end));
            }
        };
    }

    @NonNull
    private static Func1<Integer, Ceremony> createMaxHeightCeremony() {
        return new Func1<Integer, Ceremony>() {
            @Override
            public Ceremony call(@NonNull Integer timeSlotDurationMilliseconds) {
                Date start = new Date();
                Date end = new Date(start.getTime() + timeSlotDurationMilliseconds);
                return new Ceremony(
                        NO_ID,
                        DUMMY_NAME,
                        DUMMY_DESCRIPTION,
                        new TimeSlot(start, end),
                        Collections.singletonList(new Room(NO_ID, "Room"))
                );
            }
        };
    }

    @NonNull
    private static Func1<Integer, Placeholder> createMaxHeightPlaceholder() {
        return new Func1<Integer, Placeholder>() {
            @Override
            public Placeholder call(@NonNull Integer timeSlotDurationMilliseconds) {
                Date start = new Date();
                Date end = new Date(start.getTime() + timeSlotDurationMilliseconds);
                return new Placeholder(
                        NO_ID,
                        DUMMY_NAME,
                        DUMMY_DESCRIPTION,
                        new TimeSlot(start, end),
                        Collections.singletonList(new Room(NO_ID, "Room"))
                );
            }
        };
    }

    @NonNull
    private static Speaker createDummySpeaker(String speakerName) {
        return new Speaker(NO_ID, speakerName, null, null, null, null);
    }

}
