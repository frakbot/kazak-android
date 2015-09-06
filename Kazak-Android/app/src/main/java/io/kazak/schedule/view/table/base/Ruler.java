package io.kazak.schedule.view.table.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Collections;
import java.util.List;

import io.kazak.R;

public abstract class Ruler extends View {

    private static final int[] POSITION = new int[2];
    private static final int X = 0;
    private static final int Y = 1;

    @Nullable
    private RecyclerViewWrapper recyclerViewWrapper;

    @NonNull
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private float centeredBaselineShift;

    public Ruler(Context context) {
        this(context, null);
    }

    public Ruler(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.rulerDefaultStyle);
    }

    public Ruler(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.RulerDefaultStyle);
    }

    public Ruler(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        textPaint.setTextAlign(Paint.Align.CENTER);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Ruler, defStyleAttr, defStyleRes);
        setTextColor(a.getColor(R.styleable.Ruler_android_textColor, Color.BLACK));
        setTextSize(a.getDimension(R.styleable.Ruler_android_textSize, 0f));
        a.recycle();
    }

    @Nullable
    protected RecyclerViewWrapper getRecyclerViewWrapper() {
        return recyclerViewWrapper;
    }

    @ColorInt
    public int getTextColor() {
        return textPaint.getColor();
    }

    public void setTextColor(@ColorInt int color) {
        if (getTextColor() != color) {
            textPaint.setColor(color);
            invalidate();
        }
    }

    public float getTextSize() {
        return textPaint.getTextSize();
    }

    public void setTextSize(float size) {
        if (getTextSize() != size) {
            textPaint.setTextSize(size);
            computeBaselineShift();
            invalidate();
        }
    }

    protected void computeBaselineShift() {
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        centeredBaselineShift = -(fm.descent + fm.ascent) / 2f;
    }

    protected void drawVerticallyCenteredLabel(@NonNull Canvas canvas, @NonNull String label) {
        canvas.drawText(label, 0f, centeredBaselineShift, textPaint);
    }

    public void bind(@NonNull RecyclerView recyclerView) {
        if (recyclerViewWrapper != null) {
            recyclerViewWrapper.layoutManager.removeLabelsChangedListener(recyclerViewWrapper);
        }
        recyclerViewWrapper = new RecyclerViewWrapper(
                recyclerView,
                TableLayoutManager.getFromRecyclerViewOrThrow(recyclerView));
        recyclerViewWrapper.layoutManager.addLabelsChangedListener(recyclerViewWrapper);
        invalidate();
    }

    protected void onLabelsChanged() {
        invalidate();
    }

    protected int getTopOnScreen() {
        getLocationOnScreen(POSITION);
        return POSITION[Y];
    }

    protected int getLeftOnScreen() {
        getLocationOnScreen(POSITION);
        return POSITION[X];
    }

    protected class RecyclerViewWrapper implements TableLabelsChangedListener {

        private final RecyclerView recyclerView;
        private final TableLayoutManager layoutManager;

        @NonNull
        private List<String> rowsLabels = Collections.emptyList();
        private int firstRowIndex;
        private int firstRowPositionY;
        private int rowHeight;

        @NonNull
        private List<String> boundsLabels = Collections.emptyList();
        private int firstBoundIndex;
        private int firstBoundPositionX;
        private int boundsSpacing;

        public RecyclerViewWrapper(
                @NonNull RecyclerView recyclerView,
                @NonNull TableLayoutManager layoutManager) {
            this.recyclerView = recyclerView;
            this.layoutManager = layoutManager;
        }

        public int getVerticalDistanceFromRecyclerView() {
            return getRecyclerViewTopOnScreen() - getTopOnScreen();
        }

        protected int getHorizontalDistanceFromRecyclerView() {
            return getRecyclerViewLeftOnScreen() - getLeftOnScreen();
        }

        public int getRecyclerViewTopOnScreen() {
            recyclerView.getLocationOnScreen(POSITION);
            return POSITION[Y];
        }

        public int getRecyclerViewLeftOnScreen() {
            recyclerView.getLocationOnScreen(POSITION);
            return POSITION[X];
        }

        // flat model, can't be changed
        @SuppressWarnings("checkstyle:parameternumber")
        @Override
        public void onTableLabelsChanged(
                @NonNull List<String> newRowsLabels, int newFirstRowIndex, int newFirstRowPositionY, int newRowHeight,
                @NonNull List<String> newBoundsLabels, int newFirstBoundIndex, int newFirstBoundPositionX, int newBoundsSpacing) {

            this.rowsLabels = newRowsLabels;
            this.firstRowIndex = newFirstRowIndex;
            this.firstRowPositionY = newFirstRowPositionY;
            this.rowHeight = newRowHeight;

            this.boundsLabels = newBoundsLabels;
            this.firstBoundIndex = newFirstBoundIndex;
            this.firstBoundPositionX = newFirstBoundPositionX;
            this.boundsSpacing = newBoundsSpacing;

            onLabelsChanged();
        }

        @NonNull
        public List<String> getRowsLabels() {
            return rowsLabels;
        }

        public int getFirstRowIndex() {
            return firstRowIndex;
        }

        public int getFirstRowPositionY() {
            return firstRowPositionY + getVerticalDistanceFromRecyclerView();
        }

        public int getRowHeight() {
            return rowHeight;
        }

        @NonNull
        public List<String> getBoundsLabels() {
            return boundsLabels;
        }

        public int getFirstBoundIndex() {
            return firstBoundIndex;
        }

        public int getFirstBoundPositionX() {
            return firstBoundPositionX + getHorizontalDistanceFromRecyclerView();
        }

        public int getBoundsSpacing() {
            return boundsSpacing;
        }
    }

}
