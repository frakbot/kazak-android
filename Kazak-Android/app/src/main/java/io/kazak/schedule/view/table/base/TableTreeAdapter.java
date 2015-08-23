package io.kazak.schedule.view.table.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import io.kazak.model.ScheduleBound;
import io.kazak.model.ScheduleItem;
import io.kazak.model.ScheduleRow;

public abstract class TableTreeAdapter extends TableAdapterAbs {

    private final TreeSet<RangePosition<ScheduleBound>> emptySet = new TreeSet<>();

    @NonNull
    private List<? extends ScheduleItem> items = Collections.emptyList();

    @NonNull
    private TreeMap<? extends ScheduleRow, TreeSet<RangePosition<ScheduleBound>>> positions = new TreeMap<>();

    private final RangePosition<ScheduleBound> startMarker;
    private final RangePosition<ScheduleBound> endMarker;

    @Nullable
    private ScheduleBound minStart;
    @Nullable
    private ScheduleBound maxEnd;

    protected TableTreeAdapter(@NonNull TableDataHandler dataHandler) {
        super(dataHandler);
        startMarker = createRangePositionMarker();
        endMarker = createRangePositionMarker();
    }

    public void updateWith(@NonNull Data data) {
        updateWith(data.items, data.positions, data.minStart, data.maxEnd);
    }

    public void updateWith(
            @NonNull List<? extends ScheduleItem> newItems,
            @NonNull TreeMap<? extends ScheduleRow, TreeSet<RangePosition<ScheduleBound>>> newPositions,
            @Nullable ScheduleBound newMinStart, @Nullable ScheduleBound newMaxEnd) {
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
    public ScheduleItem getItem(int position) {
        return items.get(position);
    }

    @Nullable
    @Override
    public ScheduleBound getMinStart() {
        return minStart;
    }

    @Nullable
    @Override
    public ScheduleBound getMaxEnd() {
        return maxEnd;
    }

    @NonNull
    @Override
    NavigableSet<? extends ScheduleRow> getRows() {
        return positions.navigableKeySet();
    }

    @NonNull
    @Override
    NavigableSet<RangePosition<ScheduleBound>> getPositionsIn(@NonNull ScheduleRow row, @NonNull ScheduleBound start, @NonNull ScheduleBound end) {
        TreeSet<RangePosition<ScheduleBound>> rowSet = positions.get(row);
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
        private final List<? extends ScheduleItem> items;
        @NonNull
        private final TreeMap<? extends ScheduleRow, TreeSet<RangePosition<ScheduleBound>>> positions;
        @Nullable
        private final ScheduleBound minStart;
        @Nullable
        private final ScheduleBound maxEnd;

        public Data(
                @NonNull List<? extends ScheduleItem> items,
                @NonNull TreeMap<? extends ScheduleRow, TreeSet<RangePosition<ScheduleBound>>> positions,
                @Nullable ScheduleBound minStart, @Nullable ScheduleBound maxEnd) {
            this.items = items;
            this.positions = positions;
            this.minStart = minStart;
            this.maxEnd = maxEnd;
        }

    }

}
