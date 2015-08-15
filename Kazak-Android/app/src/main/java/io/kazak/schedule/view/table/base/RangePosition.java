package io.kazak.schedule.view.table.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;

import io.kazak.base.DeveloperError;

/**
 * Maps an adapter {@link #position} to a range ({@link #start}/{@link #end}).
 * <p/>
 * In order to allow extracting a subset of adapter positions from a specified range, "markers" can be used: special RangePosition objects
 * that hold no position and can only be assigned either a start or an end, and are naturally ordered at the edge of the defining range.
 *
 * @see #createMarker(Comparator)
 * @see #setStartMarker(Object)
 * @see #setEndMarker(Object)
 * @see #releaseMarker()
 */
public class RangePosition<BOUND> implements Comparable<RangePosition<BOUND>> {

    private static final int MAGIC_ODD_PRIME = 31;

    private BOUND start;
    private BOUND end;

    private final int position;

    private final Comparator<BOUND> comparator;

    RangePosition(
            @NonNull Comparator<BOUND> boundComparator, @NotNull BOUND rangeStart, @NotNull BOUND rangeEnd, int adapterPosition) {
        comparator = boundComparator;
        start = rangeStart;
        end = rangeEnd;
        position = adapterPosition;
        if (position < 0) {
            throw new DeveloperError("Position can't be negative.");
        }
    }

    // reserved for markers (see class documentation)
    private RangePosition(@NonNull Comparator<BOUND> boundComparator) {
        comparator = boundComparator;
        start = null;
        end = null;
        position = -1;
    }

    public static <BOUND> RangePosition<BOUND> createMarker(@NonNull Comparator<BOUND> boundComparator) {
        return new RangePosition<>(boundComparator);
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
    public int compareTo(@NonNull RangePosition<BOUND> another) {
        return compare(start, end, another.start, another.end);
    }

    private int compare(
            @Nullable BOUND start1, @Nullable BOUND end1,
            @Nullable BOUND start2, @Nullable BOUND end2) {

        // if start and end are both null, return -1
        if (start1 == null && end1 == null || start2 == null && end2 == null) {
            return -1;
        }

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

        // compare
        final int result;
        if (end1 == null) {
            result = compareStartMarker(comparator, start1, start2, end2);
        } else if (start1 == null) {
            result = compareEndMarker(comparator, end1, start2, end2);
        } else {
            result = compareNonMarkers(comparator, start1, end1, start2, end2);
        }
        return result * (inverse ? -1 : 1);
    }

    private int compareStartMarker(Comparator<BOUND> boundComparator, BOUND startMarker, BOUND rangeStart, BOUND rangeEnd) {
        // range starts...
        if (boundComparator.compare(rangeStart, startMarker) >= 0) {
            // ...ON or AFTER the start marker -> start marker is BEFORE (include range)
            return -1;
        } else {
            // ...BEFORE the marker, and ends...
            if (boundComparator.compare(rangeEnd, startMarker) <= 0) {
                // ...either BEFORE or ON the start marker -> start marker is AFTER (exclude range)
                return 1;
            } else {
                // ...AFTER the start marker -> start marker is BEFORE (include range)
                return -1;
            }
        }
    }

    private int compareEndMarker(Comparator<BOUND> boundComparator, BOUND endMarker, BOUND rangeStart, BOUND rangeEnd) {
        // range ends...
        if (boundComparator.compare(rangeEnd, endMarker) <= 0) {
            // ...either BEFORE or ON the end marker -> end marker is AFTER (include range)
            return 1;
        } else {
            // ...AFTER the end marker, and starts...
            if (boundComparator.compare(rangeStart, endMarker) >= 0) {
                // ...either ON or AFTER the end marker -> end marker BEFORE (exclude range)
                return -1;
            } else {
                // ...BEFORE the end marker -> end marker is AFTER (include range)
                return 1;
            }
        }
    }

    private int compareNonMarkers(
            Comparator<BOUND> boundComparator,
            @NonNull BOUND start1, @NonNull BOUND end1,
            @NonNull BOUND start2, @NonNull BOUND end2) {
        int result = boundComparator.compare(start1, start2);
        return result != 0 ? result : boundComparator.compare(end1, end2);
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
        result = MAGIC_ODD_PRIME * result + (end != null ? end.hashCode() : 0);
        return result;
    }

}
