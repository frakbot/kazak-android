package io.kazak.schedule.view.table;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import java.util.Date;

import io.kazak.model.Room;
import io.kazak.model.Talk;
import io.kazak.schedule.view.table.base.TableViewHolder;
import io.kazak.schedule.view.TalkView;

public class ScheduleTableViewHolder extends TableViewHolder<Pair<Talk, Room>, Room, Date> {

    private TalkView talkView;

    public ScheduleTableViewHolder(@NonNull TalkView talkView, @NonNull RecyclerView.Adapter adapter) {
        super(talkView, adapter);
        this.talkView = talkView;
    }

    @Override
    public void updateWith(Pair<Talk, Room> item, Room row, Date start, Date end, boolean isPlaceholder) {
        super.updateWith(item, row, start, end, isPlaceholder);
        talkView.updateWith(item.first);
    }

    TalkView getTalkView() {
        return talkView;
    }
}
