package io.kazak.schedule.view.table.base;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;

import io.kazak.base.DeveloperError;
import io.kazak.model.ScheduleBound;
import io.kazak.model.ScheduleRow;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;

public class TableLayoutManager extends RecyclerView.LayoutManager {

    private static final int VIEW_INDEX_END = -1;

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

    private final Rect tmpRect = new Rect();

    @Nullable
    private TableAdapterAbs boundAdapter;

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

    private void onDataOrSizeChanged(RecyclerView.Recycler recycler) {
        onDataOrSizeChanged(recycler, boundAdapter);
    }

    /**
     * This is just a way to make use of the generic types, don't use it directly.
     *
     * @see #onDataOrSizeChanged(android.support.v7.widget.RecyclerView.Recycler)
     */
    private void onDataOrSizeChanged(RecyclerView.Recycler recycler, @Nullable TableAdapterAbs adapter) {
        scrollX = 0;
        scrollY = 0;
        visibleUnits = (int) Math.ceil(unitsPerPixel * getWidth());

        setViewCacheSize(recycler);

        if (adapter != null) {
            ScheduleBound minStart = adapter.getMinStart();
            ScheduleBound maxEnd = adapter.getMaxEnd();
            if (minStart != null && maxEnd != null) {
                Collection<?> rows = adapter.getRows();
                scrollXRange = (int) Math.ceil(adapter.getDataHandler().getLength(minStart, maxEnd) * pixelsPerUnit) + getTotalPaddingHorizontal();
                scrollYRange = rows.size() * rowHeightPx + getTotalPaddingVertical();
                return;
            }
        }
        // else -> no data
        scrollXRange = 0;
        scrollYRange = 0;
        removeAllViews();
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
        onDataOrSizeChanged(recycler);
        fillVisibleItems(recycler);
    }

    private void fillVisibleItems(RecyclerView.Recycler recycler) {
        fillVisibleItems(recycler, boundAdapter);
    }

    /**
     * This is just a way to make use of the generic types, don't use it directly.
     *
     * @see #fillVisibleItems(android.support.v7.widget.RecyclerView.Recycler)
     */
    private void fillVisibleItems(RecyclerView.Recycler recycler, @Nullable TableAdapterAbs adapter) {
        if (adapter == null) {
            throw new DeveloperError("Adapter must be set.");
        }

        // detach all the views. some might be reattached (potentially in a different order),
        // and all those left detached will be scraped at the end.
        detachAndScrapAttachedViews(recycler);

        ScheduleBound minStart = adapter.getMinStart();
        ScheduleBound maxEnd = adapter.getMaxEnd();
        if (minStart != null && maxEnd != null) {

            NavigableSet<? extends ScheduleRow> rows = adapter.getRows();
            if (!rows.isEmpty()) {

                int parentHeight = getHeight();
                int height = rowHeightPx;

                int scrollXUnits = (int) Math.floor(unitsPerPixel * scrollX);

                TableDataHandler dataHandler = adapter.getDataHandler();

                ScheduleBound start = dataHandler.sum(minStart, scrollXUnits);
                ScheduleBound end = dataHandler.sum(start, visibleUnits);

                // this is to account for any rounding error in the conversion from "units" to "pixels"
                int shiftX = scrollX - getWidthFor(minStart, start, dataHandler);

                int y = -scrollY;
                boolean isFirstRow, isLastRow;
                int rowIndex = 0;
                int lastRowIndex = rows.size() - 1;
                int placeholdersCount = 0;

                // iterate each row, starting from the first. this is efficient for our use case (few rows, known height)
                // instead of keeping track of the first visible row and the offset.
                for (ScheduleRow row : rows) {

                    // if y is past the visible vertical range, stop
                    if (y >= parentHeight) {
                        break;
                    }

                    // only do something if it's in the visible vertical range
                    if (y + height > 0) {

                        isFirstRow = rowIndex == 0;
                        isLastRow = rowIndex == lastRowIndex;

                        // get all the views in the horizontal visible range
                        for (RangePosition<ScheduleBound> rangePosition : adapter.getPositionsIn(row, start, end)) {

                            View view = recycler.getViewForPosition(rangePosition.getPosition());

                            TableViewHolder<?, ScheduleRow, ScheduleBound> vh = adapter.getViewHolder(view);
                            TableLayoutParams lp = TableLayoutParams.getFor(view, true);

                            // update the LayoutParams based on the new view position
                            lp.setIsFirstRow(isFirstRow);
                            lp.setIsLastRow(isLastRow);
                            lp.setStartsFirst(minStart.equals(vh.getStart()));
                            lp.setEndsLast(maxEnd.equals(vh.getEnd()));
                            lp.setIsPlaceholder(vh.isPlaceholder());
                            view.setLayoutParams(lp);

                            // add the view. if it's a placeholder, add it before other views
                            // (so it gets drawn first) but keep the order between placeholders
                            int viewIndex = lp.isPlaceholder() ? placeholdersCount++ : VIEW_INDEX_END;
                            addView(view, viewIndex);

                            // calculate normal and decorated dimensions/position
                            int width = getWidthFor(vh.getStart(), vh.getEnd(), dataHandler);
                            int x = getWidthFor(start, vh.getStart(), dataHandler) + shiftX;
                            Rect decoration = tmpRect;
                            calculateItemDecorationsForChild(view, decoration);
                            int decorWidth = width - decoration.left - decoration.right;
                            int decorHeight = height - decoration.top - decoration.bottom;
                            int decorX = x + decoration.left + getTotalPaddingLeft();
                            int decorY = y + decoration.top + getTotalPaddingTop();

                            view.measure(
                                    makeMeasureSpec(decorWidth, EXACTLY),
                                    makeMeasureSpec(decorHeight, EXACTLY));
                            view.layout(decorX, decorY, decorX + decorWidth, decorY + decorHeight);
                        }
                    }
                    y += height;
                    rowIndex++;
                }

                adapter.onReleaseRowsPositionsResources();
            }
        }

        // scrap all detached views
        List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        for (RecyclerView.ViewHolder viewHolder : scrapList) {
            removeView(viewHolder.itemView);
        }
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        dx = clipScrollDelta(scrollX, dx, scrollXRange - getWidth());
        if (dx != 0) {
            scrollX += dx;
            fillVisibleItems(recycler);
        }
        return dx;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        dy = clipScrollDelta(scrollY, dy, scrollYRange - getHeight());
        if (dy != 0) {
            scrollY += dy;
            fillVisibleItems(recycler);
        }
        return dy;
    }

    private int clipScrollDelta(int currentValue, int delta, int max) {
        int newValue = currentValue + delta;
        if (newValue < 0) {
            delta = -currentValue;
        } else {
            if (max < 0) {
                delta = 0;
            } else if (newValue > max) {
                delta = max - currentValue;
            }
        }
        return delta;
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
        return getPaddingLeft() + getPaddingRight() + extraHorizontalPadding * 2;
    }

    private int getTotalPaddingVertical() {
        return getPaddingTop() + getPaddingBottom() + extraVerticalPadding * 2;
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

    private int getWidthFor(@NonNull ScheduleBound start, @NonNull ScheduleBound end, @NonNull TableDataHandler dataHandler) {
        return (int) Math.round(dataHandler.getLength(start, end) * pixelsPerUnit);
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
        if (newAdapter instanceof TableAdapterAbs) {
            boundAdapter = (TableAdapterAbs) newAdapter;
        }
        if (boundAdapter == null) {
            onDataOrSizeChanged(null);
        }
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

}
