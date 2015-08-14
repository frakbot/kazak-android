package io.kazak.schedule.view.table.base;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class TableViewHolder<I, R, C> extends RecyclerView.ViewHolder {

    I item;
    R row;
    C start;
    C end;

    boolean isPlaceholder;

    public TableViewHolder(@NonNull View view) {
        super(view);
    }

    @CallSuper
    public void updateWith(I item, R row, C start, C end, boolean isPlaceholder) {
        this.item = item;
        this.row = row;
        this.start = start;
        this.end = end;
        this.isPlaceholder = isPlaceholder;
    }

}
