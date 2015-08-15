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

import io.kazak.base.DeveloperError;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;

public class TableLayoutManager extends RecyclerView.LayoutManager {

    private final int rowHeightPx;
    private final int minSpanWidthPx;
    private final int minSpanLengthUnits;
    final int extraHorizontalPadding;
    final int extraVerticalPadding;

    private final double pixelsPerUnit;
    private final double unitsPerPixel;

    private int visibleUnits;
    private int scrollX, scrollY;
    private int scrollXRange, scrollYRange;

    private final Rect tmpRect = new Rect();

    @Nullable
    private TableAdapterAbs<?, ?, ?, ?> adapter;

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
        ensurePositive(minSpanLengthUnits, "minSpanLength");
    }

    private static void ensurePositive(float value, String name) {
        if (value <= 0) {
            throw new DeveloperError("Value of '%s' must be positive.", name);
        }
    }

    private void onDataOrSizeChanged(RecyclerView.Recycler recycler, RecyclerView.State state) {
        onDataOrSizeChanged(recycler, state, adapter);
    }

    private <BOUND> void onDataOrSizeChanged(
            RecyclerView.Recycler recycler, RecyclerView.State state, @Nullable TableAdapterAbs<?, ?, BOUND, ?> adapter) {
        scrollX = scrollY = 0;
        visibleUnits = (int) Math.ceil(unitsPerPixel * getWidth());

        if (recycler != null) {
            int numberOfVisibleRows = (int) Math.ceil((double) getHeight() / rowHeightPx);
            int numberOfVisibleUnits = (int) Math.ceil((double) getWidth() / minSpanWidthPx);
            recycler.setViewCacheSize(numberOfVisibleRows + numberOfVisibleUnits);
        }

        if (adapter != null) {
            BOUND minStart = adapter.getMinStart();
            BOUND maxEnd = adapter.getMaxEnd();
            if (minStart != null && maxEnd != null) {
                Collection<?> rows = adapter.getRows();
                scrollXRange = (int) Math.ceil(adapter.dataHandler.getLength(minStart, maxEnd) * pixelsPerUnit) + getTotalPaddingHorizontal();
                scrollYRange = rows.size() * rowHeightPx + getTotalPaddingVertical();
                return;
            }
        }
        // else -> no data
        scrollXRange = scrollYRange = 0;
        removeAllViews();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        onDataOrSizeChanged(recycler, state);
        fillVisibleItems(recycler, state);
    }

    private void fillVisibleItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        fillVisibleItems(recycler, state, adapter);
    }

    private <ROW, BOUND> void fillVisibleItems(
            RecyclerView.Recycler recycler, RecyclerView.State state, @Nullable TableAdapterAbs<?, ROW, BOUND, ?> adapter) {
        if (adapter == null) {
            throw new DeveloperError("Adapter must be set.");
        }

        detachAndScrapAttachedViews(recycler);

        BOUND minStart = adapter.getMinStart();
        BOUND maxEnd = adapter.getMaxEnd();
        if (minStart != null && maxEnd != null) {

            Collection<ROW> rows = adapter.getRows();
            if (!rows.isEmpty()) {

                int parentHeight = getHeight();
                int height = rowHeightPx;

                int scrollXUnits = (int) Math.floor(unitsPerPixel * scrollX);

                TableDataHandler<?, ROW, BOUND> dataHandler = adapter.dataHandler;

                BOUND start = dataHandler.sum(minStart, scrollXUnits);
                BOUND end = dataHandler.sum(start, visibleUnits);

                int shiftX = scrollX - getWidthFor(minStart, start, dataHandler);

                int y = -scrollY;
                boolean isFirstRow, isLastRow;
                int rowIndex = 0;
                int lastRowIndex = rows.size() - 1;
                int placeholdersCount = 0;

                for (ROW row : rows) {
                    if (y >= parentHeight) {
                        break;
                    }
                    if (y + height > 0) {

                        isFirstRow = rowIndex == 0;
                        isLastRow = rowIndex == lastRowIndex;

                        for (TableAdapterAbs.RangePosition range : adapter.getPositionsIn(row, start, end)) {

                            int pos = range.getPosition();
                            View view = recycler.getViewForPosition(pos);

                            TableViewHolder<?, ROW, BOUND> vh = adapter.getViewHolder(view);
                            TableLayoutParams lp = TableLayoutParams.getOrCreateFor(view);

                            lp.isFirstRow = isFirstRow;
                            lp.isLastRow = isLastRow;
                            lp.startsFirst = minStart.equals(vh.start);
                            lp.endsLast = maxEnd.equals(vh.end);
                            lp.isPlaceholder = vh.isPlaceholder;
                            view.setLayoutParams(lp);

                            int viewIndex = lp.isPlaceholder ? placeholdersCount++ : -1;
                            addView(view, viewIndex);

                            int width = getWidthFor(vh.start, vh.end, dataHandler);
                            int x = getWidthFor(start, vh.start, dataHandler) + shiftX;
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

    private <BOUND> int getWidthFor(@NonNull BOUND start, @NonNull BOUND end, @NonNull TableDataHandler<?, ?, BOUND> dataHandler) {
        return (int) Math.round(dataHandler.getLength(start, end) * pixelsPerUnit);
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
        if (newAdapter != null) {
            try {
                adapter = (TableAdapterAbs<?, ?, ?, ?>) newAdapter;
            } catch (ClassCastException e) {
                throw new DeveloperError(e, "Adapter must be a %s.", TableAdapterAbs.class.getSimpleName());
            }
        }
        if (adapter == null) {
            onDataOrSizeChanged(null, null);
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
