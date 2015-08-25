package io.kazak.schedule.view.table.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class TableTreeAdapter<ITEM, ROW, BOUND, VH extends TableViewHolder<ITEM, ROW, BOUND>> extends TableAdapterAbs<ITEM, ROW, BOUND, VH> {

    private final TreeSet<RangePosition<BOUND>> emptySet = new TreeSet<>();

    @NonNull
    private List<ITEM> items = Collections.emptyList();

    @NonNull
    private TreeMap<ROW, TreeSet<RangePosition<BOUND>>> positions = new TreeMap<>();

    private final RangePosition<BOUND> startMarker;
    private final RangePosition<BOUND> endMarker;

    @Nullable
    private BOUND minStart;
    @Nullable
    private BOUND maxEnd;

    protected TableTreeAdapter(@NonNull TableDataHandler<ITEM, ROW, BOUND> dataHandler) {
        super(dataHandler);
        startMarker = createRangePositionMarker();
        endMarker = createRangePositionMarker();
    }

    public void updateWith(@NonNull Data data) {
        updateWith(data.items, data.positions, data.minStart, data.maxEnd);
    }

    public void updateWith(
            @NonNull List<ITEM> newItems,
            @NonNull TreeMap<ROW, TreeSet<RangePosition<BOUND>>> newPositions,
            @Nullable BOUND newMinStart, @Nullable BOUND newMaxEnd) {
        items = newItems;
        positions = newPositions;
        minStart = newMinStart;
        maxEnd = newMaxEnd;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public ITEM getItem(int position) {
        return items.get(position);
    }

    @Nullable
    @Override
    public BOUND getMinStart() {
        return minStart;
    }

    @Nullable
    @Override
    public BOUND getMaxEnd() {
        return maxEnd;
    }

    @NonNull
    @Override
    NavigableSet<ROW> getRows() {
        return positions.navigableKeySet();
    }

    @NonNull
    @Override
    NavigableSet<RangePosition<BOUND>> getPositionsIn(@NonNull ROW row, @NonNull BOUND start, @NonNull BOUND end) {
        TreeSet<RangePosition<BOUND>> rowSet = positions.get(row);
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
        private final List<ITEM> items;
        @NonNull
        private final TreeMap<ROW, TreeSet<RangePosition<BOUND>>> positions;
        @Nullable
        private final BOUND minStart;
        @Nullable
        private final BOUND maxEnd;

        public Data(
                @NonNull List<ITEM> items,
                @NonNull TreeMap<ROW, TreeSet<RangePosition<BOUND>>> positions,
                @Nullable BOUND minStart, @Nullable BOUND maxEnd) {
            this.items = items;
            this.positions = positions;
            this.minStart = minStart;
            this.maxEnd = maxEnd;
        }

    }

}
