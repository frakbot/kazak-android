package io.kazak.schedule.view.table.base;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class TableViewHolder<ScheduleItem, ScheduleRow, ScheduleBound> extends RecyclerView.ViewHolder {

    private ScheduleItem item;
    private ScheduleRow row;
    private ScheduleBound start;
    private ScheduleBound end;

    private boolean isPlaceholder;

    public TableViewHolder(@NonNull View view) {
        super(view);
    }

    @CallSuper
    public void updateWith(ScheduleItem newItem, ScheduleRow newRow, ScheduleBound newStart, ScheduleBound newEnd, boolean newIsPlaceholder) {
        item = newItem;
        row = newRow;
        start = newStart;
        end = newEnd;
        isPlaceholder = newIsPlaceholder;
    }

    public ScheduleItem getItem() {
        return item;
    }

    public ScheduleRow getRow() {
        return row;
    }

    public ScheduleBound getStart() {
        return start;
    }

    public ScheduleBound getEnd() {
        return end;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

}
