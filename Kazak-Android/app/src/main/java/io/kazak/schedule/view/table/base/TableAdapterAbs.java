package io.kazak.schedule.view.table.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import io.kazak.base.DeveloperError;

public abstract class TableAdapterAbs<ITEM, ROW, BOUND, VH extends TableViewHolder<ITEM, ROW, BOUND>> extends RecyclerView.Adapter<VH> {

    protected static final int VIEW_TYPE_NORMAL = 0;
    protected static final int VIEW_TYPE_PLACEHOLDER = 1;

    private final TableDataHandler<ITEM, ROW, BOUND> dataHandler;

    private RecyclerView boundRecyclerView;

    protected TableAdapterAbs(@NonNull TableDataHandler<ITEM, ROW, BOUND> dh) {
        dataHandler = dh;
    }

    public abstract ITEM getItem(int position);

    @Nullable
    public abstract BOUND getMinStart();

    @Nullable
    public abstract BOUND getMaxEnd();

    @NonNull
    abstract Collection<ROW> getRows();

    @NonNull
    abstract Collection<RangePosition<BOUND>> getPositionsIn(@NonNull ROW row, @NonNull BOUND start, @NonNull BOUND end);

    @NonNull
    TableDataHandler<ITEM, ROW, BOUND> getDataHandler() {
        return dataHandler;
    }

    /**
     * Called from after {@link #getRows()} and/or {@link #getPositionsIn(Object, Object, Object)} in order to release any eventual resources.
     */
    protected void onReleaseRowsPositionsResources() {
    }

    /**
     * Returns the ViewHolder associated with the View, optimistically casted.
     */
    @NonNull
    TableViewHolder<ITEM, ROW, BOUND> getViewHolder(@NonNull View view) {
        RecyclerView.ViewHolder vh = boundRecyclerView.getChildViewHolder(view);
        if (vh == null) {
            throw new DeveloperError("No ViewHolder associated with this view.");
        }
        TableViewHolder<ITEM, ROW, BOUND> tvh;
        try {
            //noinspection unchecked
            tvh = (TableViewHolder<ITEM, ROW, BOUND>) vh;
        } catch (ClassCastException e) {
            throw new DeveloperError(e, "ViewHolder is not a %s.", TableViewHolder.class.getSimpleName());
        }
        // this is a trick to make sure that the generic types of the viewholder match those of the adapter
        if (tvh.getAdapter() != this) {
            throw new DeveloperError("ViewHolder is not associated with this Adapter.");
        }
        return tvh;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        ITEM item = getItem(position);
        holder.updateWith(
                item,
                dataHandler.getRowFor(item, position, this),
                dataHandler.getStartFor(item, position, this),
                dataHandler.getEndFor(item, position, this),
                dataHandler.isPlaceholder(item, position, this));
    }

    @Override
    public int getItemViewType(int position) {
        ITEM item = getItem(position);
        return dataHandler.isPlaceholder(item, position, this) ? VIEW_TYPE_PLACEHOLDER : VIEW_TYPE_NORMAL;
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
    protected RangePosition<BOUND> createRangePosition(@NotNull BOUND rangeStart, @NotNull BOUND rangeEnd, int adapterPosition) {
        return new RangePosition<>(dataHandler, rangeStart, rangeEnd, adapterPosition);
    }

    @NonNull
    protected RangePosition<BOUND> createRangePositionMarker() {
        return RangePosition.createMarker(dataHandler);
    }

    @Nullable
    public RecyclerView getRecyclerView() {
        return boundRecyclerView;
    }

}
