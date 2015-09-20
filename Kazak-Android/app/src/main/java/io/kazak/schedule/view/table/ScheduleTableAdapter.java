package io.kazak.schedule.view.table;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import io.kazak.BR;
import io.kazak.model.Event;
import io.kazak.model.Id;
import io.kazak.model.Room;
import io.kazak.model.Session;
import io.kazak.model.TimeSlot;
import io.kazak.schedule.view.EventViewType;
import io.kazak.schedule.view.ScheduleBindingState;
import io.kazak.schedule.view.ScheduleEventView;
import io.kazak.schedule.view.table.base.RangePosition;
import io.kazak.schedule.view.table.base.TableAdapterAbs;
import io.kazak.schedule.view.table.base.TableDataHandler;
import io.kazak.schedule.view.table.base.TableTreeAdapter;

public class ScheduleTableAdapter extends TableTreeAdapter<Pair<Event, Room>, Room, Date, ScheduleTableViewHolder> {

    private static final EventDataHandler EVENT_DATA_HANDLER = new EventDataHandler();
    private static final Comparator<Room> ROOM_COMPARATOR = new RoomComparator();

    private final LayoutInflater inflater;

    @NonNull
    private final ScheduleBindingState sharedBindingState = new ScheduleBindingState();

    public ScheduleTableAdapter(@NonNull Context context) {
        super(EVENT_DATA_HANDLER);
        this.inflater = LayoutInflater.from(context);
    }

    public void setListener(@Nullable ScheduleEventView.Listener newListener) {
        sharedBindingState.setListener(newListener);
    }

    @Override
    public int getItemViewType(int position) {
        Event event = getItem(position).first;
        return EventViewType.valueOf(event).getLayoutRes();
    }

    @Override
    public ScheduleTableViewHolder onCreateViewHolder(@Nullable ViewGroup parent, int viewType) {
        return createViewHolderForLayout(parent, viewType);
    }

    @NonNull
    public ScheduleTableViewHolder createViewHolderForLayout(@Nullable ViewGroup parent, @LayoutRes int layoutRes) {
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, layoutRes, parent, false);
        binding.setVariable(BR.state, sharedBindingState);
        return new ScheduleTableViewHolder(this, binding);
    }

    @NonNull
    public View getMaxHeightReferenceView(@NonNull EventViewType eventViewType, int timeSlotDurationMinutes) {
        ScheduleTableViewHolder viewHolder = createViewHolderForLayout(null, eventViewType.getLayoutRes());
        viewHolder.updateWith(eventViewType.getMaxHeightReference().call(timeSlotDurationMinutes));
        return viewHolder.itemView;
    }

    public void updateWith(@NonNull List<? extends Id> newFavorites) {
        sharedBindingState.setFavorites(newFavorites);
    }

    @NonNull
    public Data createSortedData(@NonNull List<? extends Event> events) {
        List<Pair<Event, Room>> talkRoomPairs = new ArrayList<>();
        TreeMap<Room, TreeSet<RangePosition<Date>>> map = new TreeMap<>(ROOM_COMPARATOR);
        Date minTime = null;
        Date maxTime = null;
        List<Event> eventsWithoutRooms = new ArrayList<>();
        Set<Room> rooms = new HashSet<>();
        for (Event event : events) {
            TimeSlot timeSlot = event.timeSlot();
            Date startTime = timeSlot.getStart();
            Date endTime = timeSlot.getEnd();
            if (minTime == null || minTime.compareTo(startTime) > 0) {
                minTime = startTime;
            }
            if (maxTime == null || maxTime.compareTo(endTime) < 0) {
                maxTime = endTime;
            }
            if (event instanceof Session) {
                Session session = (Session) event;
                for (Room room : session.rooms()) {
                    rooms.add(room);
                    addToItems(talkRoomPairs, map, event, room);
                }
            } else {
                eventsWithoutRooms.add(event);
            }
        }
        for (Event event : eventsWithoutRooms) {
            for (Room room : rooms) {
                addToItems(talkRoomPairs, map, event, room);
            }
        }
        return new Data(talkRoomPairs, map, minTime, maxTime);
    }

    private void addToItems(
            @NonNull List<Pair<Event, Room>> items,
            @NonNull TreeMap<Room, TreeSet<RangePosition<Date>>> map,
            @NonNull Event event,
            @NonNull Room room) {
        TreeSet<RangePosition<Date>> row = map.get(room);
        if (row == null) {
            row = new TreeSet<>();
            map.put(room, row);
        }
        TimeSlot timeSlot = event.timeSlot();
        int position = items.size();
        row.add(createRangePosition(timeSlot.getStart(), timeSlot.getEnd(), position));
        items.add(new Pair<>(event, room));
    }

    private static final class EventDataHandler implements TableDataHandler<Pair<Event, Room>, Room, Date>, Serializable {

        private final DateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.US);

        @Override
        public Room getRowFor(Pair<Event, Room> item, int position, TableAdapterAbs<Pair<Event, Room>, Room, Date, ?> adapter) {
            return item.second;
        }

        @Override
        public Date getStartFor(Pair<Event, Room> item, int position, TableAdapterAbs<Pair<Event, Room>, Room, Date, ?> adapter) {
            return item.first.timeSlot().getStart();
        }

        @Override
        public Date getEndFor(Pair<Event, Room> item, int position, TableAdapterAbs<Pair<Event, Room>, Room, Date, ?> adapter) {
            return item.first.timeSlot().getEnd();
        }

        @Override
        public boolean isPlaceholder(Pair<Event, Room> item, int position, TableAdapterAbs<Pair<Event, Room>, Room, Date, ?> adapter) {
            return EventViewType.valueOf(item.first).isPlaceholder();
        }

        @Override
        public int getLength(Date start, Date end) {
            return (int) (end.getTime() - start.getTime());
        }

        @Override
        public Date sum(Date start, int units) {
            return new Date(start.getTime() + units);
        }

        @Override
        public String getLabelForRow(Room room) {
            return room.getName();
        }

        @Override
        public String getLabelForBound(Date date) {
            return timeFormatter.format(date);
        }

        @Override
        public int compare(@NonNull Date lhs, @NonNull Date rhs) {
            return lhs.compareTo(rhs);
        }

    }

    private static final class RoomComparator implements Comparator<Room>, Serializable {

        @Override
        public int compare(@NonNull Room lhs, @NonNull Room rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }

    }

}
