package io.kazak.schedule.view.table.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

import java.util.List;

import io.kazak.R;

public class BoundsRuler extends Ruler {

    @NonNull
    private final Paint tickPaint = new Paint();

    private float tickSize;
    private int skipLabels;

    public BoundsRuler(Context context) {
        this(context, null);
    }

    public BoundsRuler(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.boundsRulerDefaultStyle);
    }

    public BoundsRuler(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.BoundsRulerDefaultStyle);
    }

    public BoundsRuler(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        tickPaint.setStyle(Paint.Style.STROKE);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BoundsRuler, defStyleAttr, defStyleRes);
        setTickSize(a.getDimension(R.styleable.BoundsRuler_tickSize, 0f));
        setTickStrokeWidth(a.getDimension(R.styleable.BoundsRuler_tickStrokeWidth, 0f));
        setTickColor(a.getColor(R.styleable.BoundsRuler_tickColor, Color.BLACK));
        setSkipLabels(a.getInt(R.styleable.BoundsRuler_skipLabels, 0));
        a.recycle();
    }

    public float getTickSize() {
        return tickSize;
    }

    public void setTickSize(float size) {
        if (tickSize != size) {
            tickSize = size;
            invalidate();
        }
    }

    public float getTickStrokeWidth() {
        return tickPaint.getStrokeWidth();
    }

    public void setTickStrokeWidth(float width) {
        if (getTickStrokeWidth() != width) {
            tickPaint.setStrokeWidth(width);
            invalidate();
        }
    }

    @ColorInt
    public int getTickColor() {
        return tickPaint.getColor();
    }

    public void setTickColor(@ColorInt int color) {
        if (getTickColor() != color) {
            tickPaint.setColor(color);
            invalidate();
        }
    }

    public int getSkipLabels() {
        return skipLabels;
    }

    public void setSkipLabels(int count) {
        if (skipLabels != count) {
            skipLabels = count;
            invalidate();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        RecyclerViewWrapper recyclerViewWrapper = getRecyclerViewWrapper();
        if (recyclerViewWrapper == null) {
            return;
        }

        canvas.save();

        float halfHeight = (float) getHeight() / 2f;
        canvas.translate(recyclerViewWrapper.getFirstBoundPositionX(), halfHeight);

        List<String> boundsLabels = recyclerViewWrapper.getBoundsLabels();
        int labelCounter = recyclerViewWrapper.getFirstBoundIndex() % (skipLabels + 1);
        for (int i = 0, labelIndex = 0, size = boundsLabels.size(); i < size; i++) {

            // draw label
            if (labelCounter == 0) {
                String label = boundsLabels.get(labelIndex);
                drawVerticallyCenteredLabel(canvas, label);
                labelIndex++;
            }

            // draw tick
            drawTick(canvas);

            canvas.translate(recyclerViewWrapper.getBoundsSpacing(), 0f);
            labelCounter = ++labelCounter % (skipLabels + 1);
        }

        canvas.restore();
    }

    private void drawTick(@NonNull Canvas canvas) {
        float halfHeight = (float) getHeight() / 2f;
        canvas.drawLine(0f, halfHeight, 0f, halfHeight - tickSize, tickPaint);
    }

}
