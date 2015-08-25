package io.kazak.schedule.view.table.base;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class TableViewHolder<ITEM, ROW, BOUND> extends RecyclerView.ViewHolder {

    private ITEM item;
    private ROW row;
    private BOUND start;
    private BOUND end;

    private boolean isPlaceholder;

    private final RecyclerView.Adapter adapter;

    public TableViewHolder(@NonNull View view, RecyclerView.Adapter adapter) {
        super(view);
        this.adapter = adapter;
    }

    @CallSuper
    public void updateWith(ITEM newItem, ROW newRow, BOUND newStart, BOUND newEnd, boolean newIsPlaceholder) {
        item = newItem;
        row = newRow;
        start = newStart;
        end = newEnd;
        isPlaceholder = newIsPlaceholder;
    }

    public ITEM getItem() {
        return item;
    }

    public ROW getRow() {
        return row;
    }

    public BOUND getStart() {
        return start;
    }

    public BOUND getEnd() {
        return end;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    @NonNull
    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }

}
