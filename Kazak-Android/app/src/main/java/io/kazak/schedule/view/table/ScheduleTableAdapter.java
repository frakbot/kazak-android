package io.kazak.schedule.view.table;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;

import io.kazak.R;
import io.kazak.base.DeveloperError;
import io.kazak.model.Id;
import io.kazak.model.Room;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import io.kazak.schedule.view.ScheduleEventView;
import io.kazak.schedule.view.TalkCardView;
import io.kazak.schedule.view.table.base.RangePosition;
import io.kazak.schedule.view.table.base.TableDataHandler;
import io.kazak.schedule.view.table.base.TableTreeAdapter;

public class ScheduleTableAdapter extends TableTreeAdapter<Pair<Talk, Room>, Room, Date, ScheduleTalkTableViewHolder> {

    private static final TableDataHandler<Pair<Talk, Room>, Room, Date> TALK_DATA_HANDLER = new TalkDataHandler();
    private static final Comparator<Room> ROOM_COMPARATOR = new RoomComparator();

    private final LayoutInflater inflater;

    @Nullable
    private ScheduleEventView.Listener listener;

    public ScheduleTableAdapter(@NonNull Context context) {
        super(TALK_DATA_HANDLER);
        this.inflater = LayoutInflater.from(context);
    }

    public void setListener(@Nullable ScheduleEventView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public ScheduleTalkTableViewHolder onCreateViewHolder(@Nullable ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return createNormalViewHolder(parent);
            case VIEW_TYPE_PLACEHOLDER:
                return createPlaceholderViewHolder(parent);
            default:
                throw new DeveloperError("Unknown view type: %d", viewType);
        }
    }

    @NonNull
    public ScheduleTalkTableViewHolder createNormalViewHolder(@Nullable ViewGroup parent) {
        return new ScheduleTalkTableViewHolder((TalkCardView) inflater.inflate(R.layout.view_schedule_talk_card, parent, false), this, listener);
    }

    @NonNull
    public ScheduleTalkTableViewHolder createPlaceholderViewHolder(@Nullable ViewGroup parent) {
        return createNormalViewHolder(parent); //TODO create the placeholder view for real
    }

    @NonNull
    public ScheduleTalkTableViewHolder createMaxHeightReferenceViewHolder() {
        return createNormalViewHolder(null);
    }

    public void updateWith(@NonNull List<? extends Id> favorites) {
        //TODO: use favorites data to render cells.
    }

    @NonNull
    public Data createSortedData(@NonNull List<Talk> talks) {
        List<Pair<Talk, Room>> talkRoomPairs = new ArrayList<>();
        TreeMap<Room, TreeSet<RangePosition<Date>>> map = new TreeMap<>(ROOM_COMPARATOR);
        Date minTime = null;
        Date maxTime = null;
        for (Talk talk : talks) {
            TimeSlot timeSlot = talk.getTimeSlot();
            Date startTime = timeSlot.getStart();
            Date endTime = timeSlot.getEnd();
            if (minTime == null || minTime.compareTo(startTime) > 0) {
                minTime = startTime;
            }
            if (maxTime == null || maxTime.compareTo(endTime) < 0) {
                maxTime = endTime;
            }
            for (Room room : talk.getRooms()) {
                TreeSet<RangePosition<Date>> row = map.get(room);
                if (row == null) {
                    row = new TreeSet<>();
                    map.put(room, row);
                }
                int position = talkRoomPairs.size();
                row.add(createRangePosition(startTime, endTime, position));
                talkRoomPairs.add(new Pair<>(talk, room));
            }
        }
        return new Data(talkRoomPairs, map, minTime, maxTime);
    }

    private static final class TalkDataHandler implements TableDataHandler<Pair<Talk, Room>, Room, Date>, Serializable {

        private final DateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.US);

        @Override
        public Room getRowFor(Pair<Talk, Room> item) {
            return item.second;
        }

        @Override
        public Date getStartFor(Pair<Talk, Room> item) {
            return item.first.getTimeSlot().getStart();
        }

        @Override
        public Date getEndFor(Pair<Talk, Room> item) {
            return item.first.getTimeSlot().getEnd();
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
        public boolean isPlaceholder(Pair<Talk, Room> item) {
            return false; //TODO
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

    };

    private static final class RoomComparator implements Comparator<Room>, Serializable {

        @Override
        public int compare(@NonNull Room lhs, @NonNull Room rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }

    }

}
