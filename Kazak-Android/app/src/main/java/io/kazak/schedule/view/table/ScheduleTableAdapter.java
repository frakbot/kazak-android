package io.kazak.schedule.view.table;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import io.kazak.R;
import io.kazak.base.DeveloperError;
import io.kazak.model.Room;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import io.kazak.schedule.view.TalkView;
import io.kazak.schedule.view.table.base.RangePosition;
import io.kazak.schedule.view.table.base.TableDataHandler;
import io.kazak.schedule.view.table.base.TableTreeAdapter;

public class ScheduleTableAdapter extends TableTreeAdapter<Talk, Room, Date, ScheduleTableViewHolder> {

    private static final TableDataHandler<Talk, Room, Date> TALK_DATA_HANDLER = new TalkDataHandler();
    private static final Comparator<Room> ROOM_COMPARATOR = new RoomComparator();

    private final LayoutInflater inflater;

    public ScheduleTableAdapter(@NonNull Context context) {
        super(TALK_DATA_HANDLER);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ScheduleTableViewHolder onCreateViewHolder(@Nullable ViewGroup parent, int viewType) {
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
    public ScheduleTableViewHolder createNormalViewHolder(@Nullable ViewGroup parent) {
        return new ScheduleTableViewHolder((TalkView) inflater.inflate(R.layout.view_talk_item, parent, false), this);
    }

    @NonNull
    public ScheduleTableViewHolder createPlaceholderViewHolder(@Nullable ViewGroup parent) {
        return createNormalViewHolder(parent); //TODO create the placeholder view for real
    }

    @NonNull
    public ScheduleTableViewHolder createMaxHeightReferenceViewHolder() {
        return createNormalViewHolder(null);
    }

    public void updateWith(@NonNull List<Talk> talks) {
        updateWith(createSortedData(talks));
    }

    @NonNull
    public Data createSortedData(@NonNull List<Talk> talks) {
        TreeMap<Room, TreeSet<RangePosition<Date>>> map = new TreeMap<>(ROOM_COMPARATOR);
        Date minTime = null;
        Date maxTime = null;
        for (int i = 0, end = talks.size(); i < end; i++) {
            Talk talk = talks.get(i);
            Room room = talk.getRoom();
            TimeSlot timeSlot = talk.getTimeSlot();
            Date startTime = timeSlot.getStart();
            Date endTime = timeSlot.getEnd();
            if (minTime == null || minTime.compareTo(startTime) > 0) {
                minTime = startTime;
            }
            if (maxTime == null || maxTime.compareTo(endTime) < 0) {
                maxTime = endTime;
            }
            TreeSet<RangePosition<Date>> row = map.get(room);
            if (row == null) {
                row = new TreeSet<>();
                map.put(room, row);
            }
            row.add(createRangePosition(startTime, endTime, i));
        }
        return new Data(talks, map, minTime, maxTime);
    }

    private static final class TalkDataHandler implements TableDataHandler<Talk, Room, Date>, Serializable {

        @Override
        public Room getRowFor(Talk item) {
            return item.getRoom();
        }

        @Override
        public Date getStartFor(Talk item) {
            return item.getTimeSlot().getStart();
        }

        @Override
        public Date getEndFor(Talk item) {
            return item.getTimeSlot().getEnd();
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
        public boolean isPlaceholder(Talk item) {
            return false; //TODO
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
