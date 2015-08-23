package io.kazak.schedule.view.table.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collection;
import java.util.NavigableSet;

import org.jetbrains.annotations.NotNull;

import io.kazak.base.DeveloperError;
import io.kazak.model.ScheduleBound;
import io.kazak.model.ScheduleItem;
import io.kazak.model.ScheduleRow;

public abstract class TableAdapterAbs extends RecyclerView.Adapter<TableViewHolder<ScheduleItem, ScheduleRow, ScheduleBound>> {

    protected static final int VIEW_TYPE_NORMAL = 0;
    protected static final int VIEW_TYPE_PLACEHOLDER = 1;

    private final TableDataHandler dataHandler;

    private RecyclerView boundRecyclerView;

    protected TableAdapterAbs(@NonNull TableDataHandler dh) {
        dataHandler = dh;
    }

    public abstract ScheduleItem getItem(int position);

    @Nullable
    public abstract ScheduleBound getMinStart();

    @Nullable
    public abstract ScheduleBound getMaxEnd();

    @NonNull
    abstract NavigableSet<? extends ScheduleRow> getRows();

    @NonNull
    abstract Collection<RangePosition<ScheduleBound>> getPositionsIn(@NonNull ScheduleRow row, @NonNull ScheduleBound start, @NonNull ScheduleBound end);

    @NonNull
    TableDataHandler getDataHandler() {
        return dataHandler;
    }

    /**
     * Called from after {@link #getRows()} and/or {@link #getPositionsIn(ScheduleRow, ScheduleBound, ScheduleBound)} in order to release any eventual resources.
     */
    protected void onReleaseRowsPositionsResources() {
    }

    /**
     * Returns the ViewHolder associated with the View, optimistically casted.
     */
    @NonNull
    TableViewHolder<ScheduleItem, ScheduleRow, ScheduleBound> getViewHolder(@NonNull View view) {
        RecyclerView.ViewHolder vh = boundRecyclerView.getChildViewHolder(view);
        if (vh == null) {
            throw new DeveloperError("No ViewHolder associated with this view.");
        }
        try {
            // suppressed as checking the correctness of the cast would be very hard and not worth it (blame type erasure)
            //noinspection unchecked
            return (TableViewHolder<ScheduleItem, ScheduleRow, ScheduleBound>) vh;
        } catch (ClassCastException e) {
            throw new DeveloperError(e, "ViewHolder is not a %s.", TableViewHolder.class.getSimpleName());
        }
    }

    @Override
    public void onBindViewHolder(TableViewHolder<ScheduleItem, ScheduleRow, ScheduleBound> holder, int position) {
        ScheduleItem item = getItem(position);
        holder.updateWith(
                item,
                dataHandler.getRowFor(item),
                dataHandler.getStartFor(item),
                dataHandler.getEndFor(item),
                dataHandler.isPlaceholder(item));
    }

    @Override
    public int getItemViewType(int position) {
        ScheduleItem item = getItem(position);
        return dataHandler.isPlaceholder(item) ? VIEW_TYPE_PLACEHOLDER : VIEW_TYPE_NORMAL;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        boundRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        boundRecyclerView = null;
    }

    @NonNull
    protected RangePosition<ScheduleBound> createRangePosition(@NotNull ScheduleBound rangeStart, @NotNull ScheduleBound rangeEnd, int adapterPosition) {
        return new RangePosition<>(dataHandler, rangeStart, rangeEnd, adapterPosition);
    }

    @NonNull
    protected RangePosition<ScheduleBound> createRangePositionMarker() {
        return RangePosition.createMarker(dataHandler);
    }

}
