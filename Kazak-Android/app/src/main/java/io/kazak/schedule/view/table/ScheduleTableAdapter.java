package io.kazak.schedule.view.table;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import io.kazak.R;
import io.kazak.model.DateBound;
import io.kazak.model.Room;
import io.kazak.model.ScheduleBound;
import io.kazak.model.ScheduleItem;
import io.kazak.model.ScheduleRow;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import io.kazak.schedule.view.TalkView;
import io.kazak.schedule.view.table.base.RangePosition;
import io.kazak.schedule.view.table.base.TableDataHandler;
import io.kazak.schedule.view.table.base.TableTreeAdapter;
import io.kazak.schedule.view.table.base.TableViewHolder;

public class ScheduleTableAdapter extends TableTreeAdapter {

    private static final TableDataHandler TALK_DATA_HANDLER = new TalkDataHandler();
    private static final Comparator<Room> ROOM_COMPARATOR = new RoomComparator();

    private final LayoutInflater inflater;

    public ScheduleTableAdapter(@NonNull Context context) {
        super(TALK_DATA_HANDLER);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public TableViewHolder<ScheduleItem, ScheduleRow, ScheduleBound> onCreateViewHolder(ViewGroup parent, int viewType) {
        TalkView talkView = (TalkView) inflater.inflate(R.layout.view_talk_item, parent, false);
        return new ScheduleTableViewHolder(talkView);
    }

    public void updateWith(@NonNull List<Talk> talks) {
        updateWith(createSortedData(talks));
    }

    @NonNull
    public Data createSortedData(@NonNull List<Talk> talks) {
        TreeMap<Room, TreeSet<RangePosition<ScheduleBound>>> map = new TreeMap<>(ROOM_COMPARATOR);
        DateBound minTime = null;
        DateBound maxTime = null;
        for (int i = 0, end = talks.size(); i < end; i++) {
            Talk talk = talks.get(i);
            Room room = talk.getRoom();
            TimeSlot timeSlot = talk.getTimeSlot();
            DateBound startTime = timeSlot.getStart();
            DateBound endTime = timeSlot.getEnd();
            if (minTime == null || minTime.compareTo(startTime) > 0) {
                minTime = startTime;
            }
            if (maxTime == null || maxTime.compareTo(endTime) < 0) {
                maxTime = endTime;
            }
            TreeSet<RangePosition<ScheduleBound>> row = map.get(room);
            if (row == null) {
                row = new TreeSet<>();
                map.put(room, row);
            }
            row.add(createRangePosition(startTime, endTime, i));
        }
        return new Data(talks, map, minTime, maxTime);
    }

    private static final class TalkDataHandler implements TableDataHandler, Serializable {

        @Override
        public ScheduleRow getRowFor(ScheduleItem item) {
            return ((Talk) item).getRoom();
        }

        @Override
        public ScheduleBound getStartFor(ScheduleItem item) {
            return ((Talk) item).getTimeSlot().getStart();
        }

        @Override
        public DateBound getEndFor(ScheduleItem item) {
            return ((Talk) item).getTimeSlot().getEnd();
        }

        @Override
        public int getLength(ScheduleBound start, ScheduleBound end) {
            return (int) (((DateBound) end).getTime() - ((DateBound) start).getTime());
        }

        @Override
        public DateBound sum(ScheduleBound start, int units) {
            return new DateBound(((DateBound) start).getTime() + units);
        }

        @Override
        public boolean isPlaceholder(ScheduleItem item) {
            return false; //TODO
        }

        @Override
        public int compare(@NonNull ScheduleBound lhs, @NonNull ScheduleBound rhs) {
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
