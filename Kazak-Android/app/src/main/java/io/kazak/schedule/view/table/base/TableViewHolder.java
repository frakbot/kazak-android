package io.kazak.schedule.view.table.base;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class TableViewHolder<ITEM, ROW, BOUND> extends RecyclerView.ViewHolder {

    ITEM item;
    ROW row;
    BOUND start;
    BOUND end;

    boolean isPlaceholder;

    public TableViewHolder(@NonNull View view) {
        super(view);
    }

    @CallSuper
    public void updateWith(ITEM item, ROW row, BOUND start, BOUND end, boolean isPlaceholder) {
        this.item = item;
        this.row = row;
        this.start = start;
        this.end = end;
        this.isPlaceholder = isPlaceholder;
    }

}
