package io.kazak.schedule.view.table.base;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.kazak.base.DeveloperError;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;

public class TableLayoutManager extends RecyclerView.LayoutManager {

    private static final Rect TMP_BOUNDS = new Rect();
    private static final int[] TMP_LOCATION = new int[2];
    private static final int LOCATION_X = 0;
    private static final int LOCATION_Y = 1;

    private static final int[] TMP_DISTANCE = TMP_LOCATION;
    private static final int DISTANCE_HORIZONTAL = 0;
    private static final int DISTANCE_VERTICAL = 1;

    private static final int VIEW_INDEX_END = -1;

    private static final int BOTH_SIDES = 2;

    private final int rowHeightPx;
    private final int minSpanWidthPx;
    private final int minSpanLengthUnits;
    private final int extraHorizontalPadding;
    private final int extraVerticalPadding;

    private final double pixelsPerUnit;
    private final double unitsPerPixel;

    private int visibleUnits;
    private int scrollX, scrollY;
    private int scrollXRange, scrollYRange;

    @Nullable
    private Ruler rowsRuler;
    @Nullable
    private Ruler boundsRuler;

    private final Rect tmpRect = new Rect();

    @NonNull
    private AdapterWrapper<?, ?> adapterWrapper = new AdapterWrapper<>(null);

    public TableLayoutManager(int rowHeightPx, int minSpanWidthPx, int minSpanLengthUnits, int extraHorizontalPadding, int extraVerticalPadding) {
        this.rowHeightPx = rowHeightPx;
        this.minSpanWidthPx = minSpanWidthPx;
        this.minSpanLengthUnits = minSpanLengthUnits;
        this.extraHorizontalPadding = extraHorizontalPadding;
        this.extraVerticalPadding = extraVerticalPadding;

        validateState();

        pixelsPerUnit = (double) minSpanWidthPx / minSpanLengthUnits;
        unitsPerPixel = (double) minSpanLengthUnits / minSpanWidthPx;
    }

    @NonNull
    public static TableLayoutManager getFromRecyclerViewOrThrow(@NonNull RecyclerView recyclerView) {
        TableLayoutManager layoutManager;
        try {
            layoutManager = (TableLayoutManager) recyclerView.getLayoutManager();
        } catch (ClassCastException e) {
            throw new DeveloperError(e, "LayoutManager must be a %s.", TableLayoutManager.class.getSimpleName());
        }
        if (layoutManager == null) {
            throw new DeveloperError("LayoutManager must not be null.");
        }
        return layoutManager;
    }

    private void validateState() {
        ensurePositive(rowHeightPx, "rowHeightPx");
        ensurePositive(minSpanWidthPx, "minSpanWidthPx");
        ensurePositive(minSpanLengthUnits, "minSpanLengthUnits");
    }

    private static void ensurePositive(float value, String name) {
        if (value <= 0) {
            throw new DeveloperError("Value of '%s' must be positive.", name);
        }
    }

    private void onDataOrSizeChanged(RecyclerView.Recycler recycler, RecyclerView.State state) {
        adapterWrapper.onDataOrSizeChanged(recycler, state);
    }

    private void setViewCacheSize(@Nullable RecyclerView.Recycler recycler) {
        if (recycler != null) {
            int numberOfVisibleRows = (int) Math.ceil((double) getHeight() / rowHeightPx);
            int numberOfVisibleUnits = (int) Math.ceil((double) getWidth() / minSpanWidthPx);
            recycler.setViewCacheSize(numberOfVisibleRows + numberOfVisibleUnits);
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        onDataOrSizeChanged(recycler, state);
        fillVisibleItems(recycler, state);
    }

    private void fillVisibleItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        adapterWrapper.fillVisibleItems(recycler, state);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        dx = clipScrollDelta(scrollX, dx, scrollXRange - getWidth());
        if (dx != 0) {
            scrollX += dx;
            fillVisibleItems(recycler, state);
        }
        return dx;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        dy = clipScrollDelta(scrollY, dy, scrollYRange - getHeight());
        if (dy != 0) {
            scrollY += dy;
            fillVisibleItems(recycler, state);
        }
        return dy;
    }

    private int clipScrollDelta(int currentValue, int delta, int max) {
        int clippedDelta = delta;
        int newValue = currentValue + delta;
        if (newValue < 0) {
            clippedDelta = -currentValue;
        } else {
            if (max < 0) {
                clippedDelta = 0;
            } else if (newValue > max) {
                clippedDelta = max - currentValue;
            }
        }
        return clippedDelta;
    }

    private void scrapDetachedViews(@NonNull RecyclerView.Recycler recycler) {
        List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        for (RecyclerView.ViewHolder viewHolder : scrapList) {
            removeView(viewHolder.itemView);
        }
    }

    private void measureAndLayoutChildWithDecorations(@NonNull View view, int width, int height, int x, int y) {
        Rect decorations = tmpRect;
        calculateItemDecorationsForChild(view, decorations);
        int decorWidth = width - decorations.left - decorations.right;
        int decorHeight = height - decorations.top - decorations.bottom;
        int decorX = x + decorations.left + getTotalPaddingLeft();
        int decorY = y + decorations.top + getTotalPaddingTop();

        measureAndLayoutChild(view, decorWidth, decorHeight, decorX, decorY);
    }

    private void measureAndLayoutChild(@NonNull View child, int width, int height, int x, int y) {
        child.measure(
                makeMeasureSpec(width, EXACTLY),
                makeMeasureSpec(height, EXACTLY));
        child.layout(x, y, x + width, y + height);
    }

    public int getExtraHorizontalPadding() {
        return extraHorizontalPadding;
    }

    public int getExtraVerticalPadding() {
        return extraVerticalPadding;
    }

    private int getTotalPaddingLeft() {
        return getPaddingLeft() + extraHorizontalPadding;
    }

    private int getTotalPaddingTop() {
        return getPaddingTop() + extraVerticalPadding;
    }

    private int getTotalPaddingHorizontal() {
        return getPaddingLeft() + getPaddingRight() + extraHorizontalPadding * BOTH_SIDES;
    }

    private int getTotalPaddingVertical() {
        return getPaddingTop() + getPaddingBottom() + extraVerticalPadding * BOTH_SIDES;
    }

    @Override
    public int computeHorizontalScrollExtent(RecyclerView.State state) {
        return getWidth();
    }

    @Override
    public int computeHorizontalScrollOffset(RecyclerView.State state) {
        return scrollX;
    }

    @Override
    public int computeHorizontalScrollRange(RecyclerView.State state) {
        return scrollXRange;
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return getHeight();
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return scrollY;
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return scrollYRange;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new TableLayoutParams();
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new TableLayoutParams(lp);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return generateDefaultLayoutParams();
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof TableLayoutParams;
    }

    public void setRowsRuler(@Nullable Ruler newRowsRuler) {
        if (rowsRuler != newRowsRuler) {
            rowsRuler = newRowsRuler;
            requestLayout();
        }
    }

    public void setBoundsRuler(@Nullable Ruler newBoundsRuler) {
        if (boundsRuler != newBoundsRuler) {
            boundsRuler = newBoundsRuler;
            requestLayout();
        }
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
        try {
            adapterWrapper = new AdapterWrapper<>((TableAdapterAbs<?, ?, ?, ?>) newAdapter);
        } catch (ClassCastException e) {
            throw new DeveloperError(e, "Adapter must be a %s.", TableAdapterAbs.class.getSimpleName());
        }

        if (adapterWrapper.adapter == null) {
            onDataOrSizeChanged(null, null);
        }
        // else -> onDataOrSizeChanged will be called through onLayoutChildren
    }

    private void getDistanceOnScreenFrom(@NonNull @Size(2) int[] distance, @NonNull Ruler ruler) {
        RecyclerView recyclerView = adapterWrapper.getRecyclerView();
        if (recyclerView == null) {
            throw new DeveloperError("Must be bound to a RecyclerView.");
        }

        recyclerView.getLocationOnScreen(TMP_LOCATION);
        int recyclerViewX = TMP_LOCATION[LOCATION_X];
        int recyclerViewY = TMP_LOCATION[LOCATION_Y];

        ruler.getBoundsOnScreen(TMP_BOUNDS);
        int rulerX = TMP_BOUNDS.left;
        int rulerY = TMP_BOUNDS.top;

        distance[DISTANCE_HORIZONTAL] = recyclerViewX - rulerX;
        distance[DISTANCE_VERTICAL] = recyclerViewY - rulerY;
    }

    private int getVerticalDistanceOnScreenFrom(@NonNull Ruler ruler) {
        getDistanceOnScreenFrom(TMP_DISTANCE, ruler);
        return TMP_DISTANCE[DISTANCE_VERTICAL];
    }

    private int getHorizontalDistanceOnScreenFrom(@NonNull Ruler ruler) {
        getDistanceOnScreenFrom(TMP_DISTANCE, ruler);
        return TMP_DISTANCE[DISTANCE_HORIZONTAL];
    }

    private class AdapterWrapper<ROW, BOUND> {

        @Nullable
        private final TableAdapterAbs<?, ROW, BOUND, ?> adapter;

        public AdapterWrapper(@Nullable TableAdapterAbs<?, ROW, BOUND, ?> adapter) {
            this.adapter = adapter;
        }

        private void onDataOrSizeChanged(RecyclerView.Recycler recycler, RecyclerView.State state) {
            scrollX = 0;
            scrollY = 0;
            visibleUnits = (int) Math.ceil(unitsPerPixel * getWidth());

            setViewCacheSize(recycler);

            if (adapter != null) {
                BOUND minStart = adapter.getMinStart();
                BOUND maxEnd = adapter.getMaxEnd();
                if (minStart != null && maxEnd != null) {
                    int horizontalRangeMinusPadding = (int) Math.ceil(adapter.getDataHandler().getLength(minStart, maxEnd) * pixelsPerUnit);
                    int verticalRangeMinusPadding = adapter.getRows().size() * rowHeightPx;
                    scrollXRange = horizontalRangeMinusPadding + getTotalPaddingHorizontal();
                    scrollYRange = verticalRangeMinusPadding + getTotalPaddingVertical();
                    return;
                }
            }
            // else -> no data
            scrollXRange = 0;
            scrollYRange = 0;
            removeAllViews();
        }

        private void fillVisibleItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
            if (adapter == null) {
                throwAdapterNotSetException();
            }

            // detach all the views. some might be reattached (potentially in a different order),
            // and all those left detached will be scraped at the end.
            detachAndScrapAttachedViews(recycler);

            BOUND totalDataStartBound = adapter.getMinStart();
            BOUND totalDataEndBound = adapter.getMaxEnd();
            if (totalDataStartBound != null && totalDataEndBound != null) {
                fillVisibleItemsWithDetachedViews(totalDataStartBound, totalDataEndBound, recycler);
            }

            scrapDetachedViews(recycler);
        }

        private void fillVisibleItemsWithDetachedViews(
                @NonNull BOUND totalDataStartBound, @NonNull BOUND totalDataEndBound,
                @NonNull RecyclerView.Recycler recycler) {

            if (adapter == null) {
                throwAdapterNotSetException();
            }

            int scrollXUnits = (int) Math.floor(unitsPerPixel * scrollX);

            int visibleTop = 0;
            int visibleBottom = getHeight();

            BOUND visibleStartBound = sum(totalDataStartBound, scrollXUnits);
            BOUND visibleEndBound = sum(visibleStartBound, visibleUnits);

            fillVisibleItemsWithDetachedViews(
                    totalDataStartBound, totalDataEndBound,
                    visibleStartBound, visibleEndBound,
                    visibleTop, visibleBottom,
                    recycler);
        }

        private void fillVisibleItemsWithDetachedViews(
                @NonNull BOUND totalDataStartBound, @NonNull BOUND totalDataEndBound,
                @NonNull BOUND visibleStartBound, @NonNull BOUND visibleEndBound,
                int visibleTop, int visibleBottom,
                @NonNull RecyclerView.Recycler recycler) {

            if (adapter == null) {
                throwAdapterNotSetException();
            }

            Collection<ROW> rows = adapter.getRows();
            if (rows.isEmpty()) {
                return;
            }

            // this is to account for any rounding error in the conversion from "units" to "pixels"
            int roundingErrorX = scrollX - getLengthPx(totalDataStartBound, visibleStartBound);

            int y = -scrollY;
            boolean isFirstRow, isLastRow;
            int rowIndex = 0;
            int lastRowIndex = rows.size() - 1;
            int placeholdersCount = 0;
            boolean isVisibleVerticalRange = false;

            int firstRowIndex = -1;
            int firstRowPositionY = 0;
            List<String> rowsLabels = initializeRowsLabels();

            // iterate each row, starting from the first. this is efficient for our use case (few rows, known height),
            // instead of keeping track of the first visible row plus its offset.
            for (ROW row : rows) {

                if (y >= visibleBottom) {
                    break;
                }

                if (!isVisibleVerticalRange && y + rowHeightPx > visibleTop) {
                    isVisibleVerticalRange = true;
                    firstRowIndex = rowIndex;
                    firstRowPositionY = y;
                }
                if (isVisibleVerticalRange) {

                    isFirstRow = rowIndex == 0;
                    isLastRow = rowIndex == lastRowIndex;

                    addRowLabel(rowsLabels, row);

                    // get all the views in the horizontal visible range
                    for (RangePosition<BOUND> rangePosition : adapter.getPositionsIn(row, visibleStartBound, visibleEndBound)) {

                        View view = recycler.getViewForPosition(rangePosition.getPosition());
                        TableViewHolder<?, ROW, BOUND> viewHolder = adapter.getViewHolder(view);
                        updateLayoutParamsForView(viewHolder, totalDataStartBound, totalDataEndBound, isFirstRow, isLastRow);

                        // add the view. if it's a placeholder, add it before other views
                        // (so it gets drawn first) but keep the order between placeholders
                        int viewIndex = viewHolder.isPlaceholder() ? placeholdersCount++ : VIEW_INDEX_END;
                        addView(view, viewIndex);

                        int width = getLengthPx(viewHolder.getStart(), viewHolder.getEnd());
                        int x = getLengthPx(visibleStartBound, viewHolder.getStart()) + roundingErrorX;
                        measureAndLayoutChildWithDecorations(view, width, rowHeightPx, x, y);
                    }
                }
                y += rowHeightPx;
                rowIndex++;
            }

            updateRowsRuler(firstRowIndex, firstRowPositionY, rowsLabels);
            updateBoundsRuler(totalDataStartBound, visibleStartBound);

            adapter.onReleaseRowsPositionsResources();
        }

        @Nullable
        private List<String> initializeRowsLabels() {
            if (rowsRuler != null) {
                int visibleRowsCount = getHeight() / rowHeightPx + 1;
                return new ArrayList<>(visibleRowsCount);
            }
            return null; // nullable order to get compile-time warnings when trying to add items
        }

        private void addRowLabel(@Nullable List<String> rowsLabels, @NonNull ROW row) {
            if (rowsRuler != null && rowsLabels != null) {
                rowsLabels.add(getRowLabel(row));
            }
        }

        private void updateLayoutParamsForView(
                @NonNull TableViewHolder<?, ROW, BOUND> viewHolder,
                @NonNull BOUND totalDataStartBound, @NonNull BOUND totalDataEndBound,
                boolean isFirstRow, boolean isLastRow) {

            View view = viewHolder.itemView;
            TableLayoutParams layoutParams = TableLayoutParams.getFor(view, true);
            layoutParams.setIsFirstRow(isFirstRow);
            layoutParams.setIsLastRow(isLastRow);
            layoutParams.setStartsFirst(totalDataStartBound.equals(viewHolder.getStart()));
            layoutParams.setEndsLast(totalDataEndBound.equals(viewHolder.getEnd()));
            layoutParams.setIsPlaceholder(viewHolder.isPlaceholder());
            view.setLayoutParams(layoutParams);
        }

        private void updateRowsRuler(int firstRowIndex, int firstRowPositionY, @Nullable List<String> rowsLabels) {
            if (rowsRuler != null && rowsLabels != null) {
                int distanceFromRuler = getVerticalDistanceOnScreenFrom(rowsRuler);
                rowsRuler.onLabelsChanged(rowsLabels, firstRowIndex, firstRowPositionY + distanceFromRuler, rowHeightPx);
            }
        }

        private void updateBoundsRuler(@NonNull BOUND totalDataStartBound, @NonNull BOUND visibleStartBound) {
            if (boundsRuler != null) {
                int distanceFromRuler = getHorizontalDistanceOnScreenFrom(boundsRuler);
                int firstBoundIndex = getLengthUnits(totalDataStartBound, visibleStartBound) / minSpanLengthUnits;
                BOUND firstBoundTick = sum(totalDataStartBound, firstBoundIndex * minSpanLengthUnits);
                int firstBoundPositionX = getLengthPx(visibleStartBound, firstBoundTick) + extraHorizontalPadding + distanceFromRuler;
                int visibleBoundsCount = getWidth() / minSpanWidthPx + 2;
                List<String> boundsLabels = new ArrayList<>(visibleBoundsCount);
                computeBoundsLabels(firstBoundTick, visibleBoundsCount, boundsLabels);
                boundsRuler.onLabelsChanged(boundsLabels, firstBoundIndex, firstBoundPositionX, minSpanWidthPx);
            }
        }

        private void computeBoundsLabels(@NonNull BOUND firstBoundTick, int visibleBoundsCount, @NonNull List<String> boundsLabels) {
            BOUND boundTick = firstBoundTick;
            for (int i = 0; i < visibleBoundsCount; i++) {
                boundsLabels.add(getBoundLabel(boundTick));
                boundTick = sum(boundTick, minSpanLengthUnits);
            }
        }

        private int getLengthUnits(@NonNull BOUND start, @NonNull BOUND end) {
            if (adapter == null) {
                throwAdapterNotSetException();
            }
            return adapter.getDataHandler().getLength(start, end);
        }

        private int getLengthPx(@NonNull BOUND start, @NonNull BOUND end) {
            if (adapter == null) {
                throwAdapterNotSetException();
            }
            return (int) Math.round(getLengthUnits(start, end) * pixelsPerUnit);
        }

        @NonNull
        private BOUND sum(@NonNull BOUND start, int units) {
            if (adapter == null) {
                throwAdapterNotSetException();
            }
            return adapter.getDataHandler().sum(start, units);
        }

        private String getRowLabel(@NonNull ROW row) {
            if (adapter == null) {
                throwAdapterNotSetException();
            }
            return adapter.getDataHandler().getLabelForRow(row);
        }

        private String getBoundLabel(@NonNull BOUND bound) {
            if (adapter == null) {
                throwAdapterNotSetException();
            }
            return adapter.getDataHandler().getLabelForBound(bound);
        }

        private void throwAdapterNotSetException() {
            throw new DeveloperError("Adapter is not set.");
        }

        @Nullable
        private RecyclerView getRecyclerView() {
            return adapter == null ? null : adapter.getRecyclerView();
        }

    }

}
