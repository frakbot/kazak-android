package io.kazak.schedule.view.table;

import android.support.annotation.NonNull;

import io.kazak.model.ScheduleBound;
import io.kazak.model.ScheduleItem;
import io.kazak.model.ScheduleRow;
import io.kazak.model.Talk;
import io.kazak.schedule.view.TalkView;
import io.kazak.schedule.view.table.base.TableViewHolder;

public class ScheduleTableViewHolder extends TableViewHolder<ScheduleItem, ScheduleRow, ScheduleBound> {

    private TalkView talkView;

    public ScheduleTableViewHolder(@NonNull TalkView talkView) {
        super(talkView);
        this.talkView = talkView;
    }

    @Override
    public void updateWith(ScheduleItem item, ScheduleRow row, ScheduleBound start, ScheduleBound end, boolean isPlaceholder) {
        super.updateWith(item, row, start, end, isPlaceholder);
        talkView.updateWith((Talk) item);
    }

}
