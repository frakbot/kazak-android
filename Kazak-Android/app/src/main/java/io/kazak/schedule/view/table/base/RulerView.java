package io.kazak.schedule.view.table.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import io.kazak.R;
import io.kazak.base.DeveloperError;

public class RulerView extends View implements Ruler {

    private static final int[] TMP_LOCATION = new int[2];
    private static final int LOCATION_X = 0;
    private static final int LOCATION_Y = 1;

    private static final float VERTICALLY_COUNTER_CLOCKWISE = -90f;
    private static final int BACKWARD = -1;
    private static final int FORWARD = 1;

    // keep in sync with attrs-ruler.xml
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @MagicConstant(valuesFromClass = Orientation.class)
    public @interface Orientation {
        int HORIZONTAL = 0;
        int VERTICAL = 1;
    }

    // keep in sync with attrs-ruler.xml
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @MagicConstant(valuesFromClass = AlignLabel.class)
    public @interface AlignLabel {
        int ON_TICK = 0;
        int BETWEEN_TICKS = 1;
    }

    @NonNull
    private final TextPaint textPaint;

    @NonNull
    private final Paint tickPaint;

    private float centeredBaselineShift;

    private float tickSize;

    @RulerView.Orientation
    private int orientation;

    @RulerView.AlignLabel
    private int alignLabel;

    @NonNull
    private List<String> labels = Collections.emptyList();
    private int firstPositionPx;
    private int ticksSpacingPx;

    public RulerView(Context context) {
        this(context, null);
    }

    public RulerView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.rulerDefaultStyle);
    }

    public RulerView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.RulerDefaultStyle);
    }

    public RulerView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr);
        super.setWillNotDraw(false);

        textPaint = createTextPaint();
        tickPaint = createTickPaint();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RulerView, defStyleAttr, defStyleRes);
        setTextColor(a.getColor(R.styleable.RulerView_android_textColor, Color.BLACK));
        setTextSize(a.getDimension(R.styleable.RulerView_android_textSize, 0f));
        setTickSize(a.getDimension(R.styleable.RulerView_tickSize, 0f));
        setTickStrokeWidth(a.getDimension(R.styleable.RulerView_tickStrokeWidth, 0f));
        setTickColor(a.getColor(R.styleable.RulerView_tickColor, Color.BLACK));
        parseXmlEnumsFrom(a);
        a.recycle();
    }

    @SuppressWarnings("MagicConstant") // getInt doesn't know anything about the types, but the setters will check if they're correct
    private void parseXmlEnumsFrom(@NotNull TypedArray a) {
        setOrientation(a.getInt(R.styleable.RulerView_orientation, Orientation.HORIZONTAL));
        setAlignLabel(a.getInt(R.styleable.RulerView_alignLabel, AlignLabel.ON_TICK));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int canvasSave = canvas.save();

        int height;
        int directionX;

        if (orientation == Orientation.VERTICAL) {
            canvas.rotate(VERTICALLY_COUNTER_CLOCKWISE);
            height = getWidth();
            directionX = BACKWARD;
        } else {
            height = getHeight();
            directionX = FORWARD;
        }

        float halfHeight = (float) height / 2f;

        canvas.translate(directionX * firstPositionPx, halfHeight);
        if (alignLabel == AlignLabel.BETWEEN_TICKS) {
            canvas.translate(directionX * (float) ticksSpacingPx / 2f, 0f);
        }

        for (String label : labels) {
            drawVerticallyCenteredLabel(canvas, label);
            drawTick(canvas, 0f, halfHeight, 0f, halfHeight - getTickSize());
            canvas.translate(directionX * ticksSpacingPx, 0);
        }

        canvas.restoreToCount(canvasSave);
    }

    public void onLabelsChanged(@NonNull List<String> newLabels, int newFirstIndex, int newFirstPositionPx, int newTicksSpacingPx) {
        labels = getEllipsizedLabels(newLabels, newTicksSpacingPx);
        firstPositionPx = newFirstPositionPx;
        ticksSpacingPx = newTicksSpacingPx;
        invalidate();
    }

    @Override
    public void getBoundsOnScreen(@NonNull Rect bounds) {
        getLocationOnScreen(TMP_LOCATION);
        int x = TMP_LOCATION[LOCATION_X];
        int y = TMP_LOCATION[LOCATION_Y];
        bounds.set(x, y, x + getWidth(), y + getHeight());
    }

    private void computeBaselineShift() {
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        centeredBaselineShift = -(fm.descent + fm.ascent) / 2f;
    }

    private void drawTick(@NonNull Canvas canvas, float startX, float startY, float stopX, float stopY) {
        canvas.drawLine(startX, startY, stopX, stopY, tickPaint);
    }

    private void drawVerticallyCenteredLabel(@NonNull Canvas canvas, @NonNull String label) {
        canvas.drawText(label, 0f, centeredBaselineShift, textPaint);
    }

    private List<String> getEllipsizedLabels(@NotNull List<String> newLabels, float newTicksSpacingPx) {
        List<String> ellipsizedLabels = new ArrayList<>(newLabels.size());
        for (String label : newLabels) {
            CharSequence ellipsizedLabel = TextUtils.ellipsize(label, textPaint, newTicksSpacingPx, TextUtils.TruncateAt.MIDDLE);
            ellipsizedLabels.add(ellipsizedLabel.toString());
        }
        return ellipsizedLabels;
    }

    @NonNull
    private TextPaint createTextPaint() {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        return paint;
    }

    @NonNull
    private Paint createTickPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        return paint;
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

    @RulerView.Orientation
    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(@RulerView.Orientation int newOrientation) {
        if (orientation != newOrientation) {
            switch (orientation) {
                default:
                    throw new DeveloperError("Unsupported orientation value.");

                case Orientation.HORIZONTAL:
                case Orientation.VERTICAL:
                    orientation = newOrientation;
                    invalidate();
                    break;
            }
        }
    }

    @RulerView.AlignLabel
    public int getAlignLabel() {
        return alignLabel;
    }

    public void setAlignLabel(@RulerView.AlignLabel int newAlignLabel) {
        if (alignLabel != newAlignLabel) {
            switch (alignLabel) {
                default:
                    throw new DeveloperError("Unsupported align label value.");

                case AlignLabel.ON_TICK:
                case AlignLabel.BETWEEN_TICKS:
                    alignLabel = newAlignLabel;
                    invalidate();
                    break;
            }
        }
    }

}
