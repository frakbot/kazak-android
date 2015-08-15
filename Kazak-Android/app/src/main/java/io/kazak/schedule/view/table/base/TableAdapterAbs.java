package io.kazak.schedule.view.table.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collection;
import java.util.Comparator;

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
    abstract Collection<RangePosition> getPositionsIn(@NonNull ROW row, @NonNull BOUND start, @NonNull BOUND end);

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
        try {
            // suppressed as checking the correctness of the cast would be very hard and not worth it (blame type erasure)
            //noinspection unchecked
            return (TableViewHolder<ITEM, ROW, BOUND>) vh;
        } catch (ClassCastException e) {
            throw new DeveloperError(e, "ViewHolder is not a %s.", TableViewHolder.class.getSimpleName());
        }
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        ITEM item = getItem(position);
        holder.updateWith(
                item,
                dataHandler.getRowFor(item),
                dataHandler.getStartFor(item),
                dataHandler.getEndFor(item),
                dataHandler.isPlaceholder(item));
    }

    @Override
    public int getItemViewType(int position) {
        ITEM item = getItem(position);
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
    protected RangePosition makeRangePositionMarker() {
        return new RangePosition();
    }

    /**
     * Maps an adapter {@link #position} to a range ({@link #start}/{@link #end}).
     * <p/>
     * In order to allow extracting a subset of adapter positions from a specified range, "markers" can be used: special RangePosition objects
     * that hold no position and can only be assigned either a start or an end, and are naturally ordered at the edge of the defining range.
     *
     * @see #makeRangePositionMarker()
     * @see #setStartMarker(Object)
     * @see #setEndMarker(Object)
     * @see #releaseMarker()
     */
    public class RangePosition implements Comparable<RangePosition> {

        private BOUND start;
        private BOUND end;

        private final int position;

        public RangePosition(@NotNull BOUND s, @NotNull BOUND e, int p) {
            start = s;
            end = e;
            position = p;
            if (position < 0) {
                throw new DeveloperError("Position can't be negative.");
            }
        }

        // reserved for markers (see class documentation)
        private RangePosition() {
            start = null;
            end = null;
            position = -1;
        }

        protected boolean isMarker() {
            return position < 0;
        }

        protected void setStartMarker(@NonNull BOUND startMarker) {
            start = startMarker;
            end = null;
        }

        protected void setEndMarker(@NonNull BOUND endMarker) {
            end = endMarker;
            start = null;
        }

        /**
         * Releases any reference to start/end used by a marker.
         */
        protected void releaseMarker() {
            if (!isMarker()) {
                throw new DeveloperError("Can only release markers.");
            }
            start = null;
            end = null;
        }

        @NotNull
        public BOUND getStart() {
            return start;
        }

        @NotNull
        public BOUND getEnd() {
            return end;
        }

        public int getPosition() {
            return position;
        }

        @Override
        public int compareTo(@NonNull RangePosition another) {
            return compare(start, end, another.start, another.end);
        }

        private int compare(
                @Nullable BOUND start1, @Nullable BOUND end1,
                @Nullable BOUND start2, @Nullable BOUND end2) {

            // if start and end are both null, return -1
            if (start1 == null && end1 == null || start2 == null && end2 == null) {
                return -1;
            }

            Comparator<BOUND> comparator = TableAdapterAbs.this.dataHandler;

            boolean firstIsMarker = start1 == null || end1 == null;
            boolean secondIsMarker = start2 == null || end2 == null;

            // if both are markers, return comparison
            if (firstIsMarker && secondIsMarker) {
                if (end1 == null) {
                    return comparator.compare(start1, end2 == null ? start2 : end2);
                } else {
                    return comparator.compare(end1, start2 == null ? end2 : start2);
                }
            }

            // marker must be before values
            boolean inverse = false;
            if (secondIsMarker) {
                inverse = true;
                BOUND tmpStart2 = start2;
                BOUND tmpEnd2 = end2;
                start2 = start1;
                end2 = end1;
                start1 = tmpStart2;
                end1 = tmpEnd2;
            }

            // start must be before end
            if (comparator.compare(start2, end2) > 0) {
                BOUND tmpStart2 = start2;
                start2 = end2;
                end2 = tmpStart2;
            }

            BOUND start = start2;
            BOUND end = end2;

            // compare
            final int result;
            if (end1 == null) {
                result = compareStartMarker(comparator, start1, start, end);
            } else if (start1 == null) {
                result = compareEndMarker(comparator, end1, start, end);
            } else {
                result = compareNonMarkers(comparator, start1, end1, start2, end2);
            }
            return result * (inverse ? -1 : 1);
        }

        private int compareStartMarker(Comparator<BOUND> comparator, BOUND startMarker, BOUND start, BOUND end) {
            // range starts...
            if (comparator.compare(start, startMarker) >= 0) {
                // ...ON or AFTER the start marker -> start marker is BEFORE (include range)
                return -1;
            } else {
                // ...BEFORE the marker, and ends...
                if (comparator.compare(end, startMarker) <= 0) {
                    // ...either BEFORE or ON the start marker -> start marker is AFTER (exclude range)
                    return 1;
                } else {
                    // ...AFTER the start marker -> start marker is BEFORE (include range)
                    return -1;
                }
            }
        }

        private int compareEndMarker(Comparator<BOUND> comparator, BOUND endMarker, BOUND start, BOUND end) {
            // range ends...
            if (comparator.compare(end, endMarker) <= 0) {
                // ...either BEFORE or ON the end marker -> end marker is AFTER (include range)
                return 1;
            } else {
                // ...AFTER the end marker, and starts...
                if (comparator.compare(start, endMarker) >= 0) {
                    // ...either ON or AFTER the end marker -> end marker BEFORE (exclude range)
                    return -1;
                } else {
                    // ...BEFORE the end marker -> end marker is AFTER (include range)
                    return 1;
                }
            }
        }

        private int compareNonMarkers(
                Comparator<BOUND> comparator,
                @NonNull BOUND start1, @NonNull BOUND end1,
                @NonNull BOUND start2, @NonNull BOUND end2) {
            int result = comparator.compare(start1, start2);
            return result != 0 ? result : comparator.compare(end1, end2);
        }

        // auto-generated, ignores position
        @SuppressWarnings({"SimplifiableIfStatement", "unchecked"})
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            RangePosition that = (RangePosition) o;

            if (start != null ? !start.equals(that.start) : that.start != null) {
                return false;
            }
            return !(end != null ? !end.equals(that.end) : that.end != null);
        }

        // auto-generated, ignores position
        @Override
        public int hashCode() {
            int result = start != null ? start.hashCode() : 0;
            result = 31 * result + (end != null ? end.hashCode() : 0);
            return result;
        }

    }

}
