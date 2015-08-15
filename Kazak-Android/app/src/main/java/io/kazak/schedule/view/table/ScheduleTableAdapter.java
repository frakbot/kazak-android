package io.kazak.schedule.view.table;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import io.kazak.R;
import io.kazak.model.Room;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import io.kazak.schedule.view.TalkView;
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
    public ScheduleTableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ScheduleTableViewHolder((TalkView) inflater.inflate(R.layout.view_talk_item, parent, false));
    }

    public void updateWith(@NonNull List<Talk> talks) {
        updateWith(createSortedData(talks));
    }

    @NonNull
    public Data createSortedData(@NonNull List<Talk> talks) {
        TreeMap<Room, TreeSet<RangePosition>> map = new TreeMap<>(ROOM_COMPARATOR);
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
            TreeSet<RangePosition> row = map.get(room);
            RangePosition tp = new RangePosition(startTime, endTime, i);
            if (row == null) {
                row = new TreeSet<>();
                map.put(room, row);
            }
            row.add(tp);
        }
        return new Data(talks, map, minTime, maxTime);
    }

    private static final class TalkDataHandler implements TableDataHandler<Talk, Room, Date> {

        @Override
        public Room getRowFrom(Talk item) {
            return item.getRoom();
        }

        @Override
        public Date getStartFrom(Talk item) {
            return item.getTimeSlot().getStart();
        }

        @Override
        public Date getEndFrom(Talk item) {
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

    private static final class RoomComparator implements Comparator<Room> {

        @Override
        public int compare(Room lhs, Room rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }

    }

}
