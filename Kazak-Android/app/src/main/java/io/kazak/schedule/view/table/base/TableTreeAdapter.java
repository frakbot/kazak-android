package io.kazak.schedule.view.table.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class TableTreeAdapter<I, R, C, VH extends TableViewHolder<I, R, C>> extends TableAdapterAbs<I, R, C, VH> {

    private final TreeSet<RangePosition> emptySet = new TreeSet<>();

    @NonNull
    List<I> items = Collections.emptyList();

    @NonNull
    private TreeMap<R, TreeSet<RangePosition>> positions = new TreeMap<>();

    private final RangePosition startMarker = makeRangePositionMarker();
    private final RangePosition endMarker = makeRangePositionMarker();

    @Nullable
    private C minStart;
    @Nullable
    private C maxEnd;

    protected TableTreeAdapter(@NonNull TableDataHandler<I, R, C> dataHandler) {
        super(dataHandler);
    }

    public void updateWith(@NonNull Data data) {
        updateWith(data.items, data.positions, data.minStart, data.maxEnd);
    }

    public void updateWith(
            @NonNull List<I> items,
            @NonNull TreeMap<R, TreeSet<RangePosition>> positions,
            @Nullable C minStart, @Nullable C maxEnd) {
        this.items = items;
        this.positions = positions;
        this.minStart = minStart;
        this.maxEnd = maxEnd;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public I getItem(int position) {
        return items.get(position);
    }

    @Nullable
    @Override
    public C getMinStart() {
        return minStart;
    }

    @Nullable
    @Override
    public C getMaxEnd() {
        return maxEnd;
    }

    @NonNull
    @Override
    NavigableSet<R> getRows() {
        return positions.navigableKeySet();
    }

    @NonNull
    @Override
    NavigableSet<RangePosition> getPositionsIn(@NonNull R row, @NonNull C start, @NonNull C end) {
        TreeMap<R, TreeSet<RangePosition>> m = positions;
        TreeSet<RangePosition> rowSet = m.get(row);
        if (rowSet == null || rowSet.isEmpty()) {
            return emptySet;
        }
        startMarker.setStartMarker(start);
        endMarker.setEndMarker(end);
        return rowSet.subSet(startMarker, false, endMarker, false);
    }

    @Override
    protected void onReleaseRowsPositionsResources() {
        startMarker.releaseMarker();
        endMarker.releaseMarker();
    }

    public final class Data {

        @NonNull
        private final List<I> items;
        @NonNull
        private final TreeMap<R, TreeSet<RangePosition>> positions;
        @Nullable
        private final C minStart;
        @Nullable
        private final C maxEnd;

        public Data(
                @NonNull List<I> items,
                @NonNull TreeMap<R, TreeSet<RangePosition>> positions,
                @Nullable C minStart, @Nullable C maxEnd) {
            this.items = items;
            this.positions = positions;
            this.minStart = minStart;
            this.maxEnd = maxEnd;
        }

    }

}
