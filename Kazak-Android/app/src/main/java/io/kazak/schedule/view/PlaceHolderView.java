package io.kazak.schedule.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.kazak.R;
import io.kazak.base.DeveloperError;
import io.kazak.model.Placeholder;
import io.kazak.model.TimeSlot;

public class PlaceHolderView extends ViewGroup {

    private static final String TIMESLOT_BOUND_PATTERN = "HH:mm";
    private static final String TIMESLOT_TEMPLATE = "%1$s—%2$s";
    private static final int UNSPECIFIED_MEASURE_SPEC = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

    private final DateFormat dateFormat;

    private final Paint trackBgPaint;
    private final RectF trackLineBounds;
    private final int trackLineHeightPx;
    private final int trackLineCornerRadiusPx;

    private TextView timeView;
    private TextView titleView;

    public PlaceHolderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaceHolderView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.CeremonyViewDefaultStyle);
    }

    public PlaceHolderView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr);

        dateFormat = new SimpleDateFormat(TIMESLOT_BOUND_PATTERN, Locale.UK);
        trackLineBounds = new RectF();
        trackBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PlaceHolderView, defStyleAttr, defStyleRes);
        trackLineHeightPx = a.getDimensionPixelSize(R.styleable.PlaceHolderView_trackLineHeight, 0);
        trackLineCornerRadiusPx = a.getDimensionPixelSize(R.styleable.PlaceHolderView_trackLineCornerRadius, 0);
        a.recycle();

        super.setWillNotDraw(false);
        super.setClipToPadding(false);
    }

    @Override
    public void setWillNotDraw(boolean willNotDraw) {
        // This is because otherwise onDraw() wouldn't get called.
        throw new DeveloperError("Nein nein nein nein nein! ಠ_ಠ");
    }

    @Override
    public void setClipToPadding(boolean willNotDraw) {
        // This is because otherwise the favorite button would get clipped
        throw new DeveloperError("Nein nein nein nein nein! ಠ_ಠ");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.merge_schedule_placeholder_view_contents, this);
        timeView = (TextView) findViewById(R.id.session_time);
        titleView = (TextView) findViewById(R.id.session_title);
    }

    @Override
    protected MarginLayoutParams generateLayoutParams(@NonNull LayoutParams lp) {
        return new MarginLayoutParams(lp);
    }

    @Override
    public MarginLayoutParams generateLayoutParams(@NonNull AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected MarginLayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Fail fast, and optimize (EXACTLY, EXACTLY) scenario
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            throw new DeveloperError("This view only supports EXACTLY for the widthMeasureSpec");
        }

        int availableWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingStart() - getPaddingEnd();

        // Measure the first row of content: [TIME] --- [TRACK LABEL]? - [TRACK DRAWABLE]?
        measureAndUpdateFirstRowContent(availableWidth);
        int totalHeight = getPaddingTop() + timeView.getMeasuredHeight() + getVerticalMarginsFor(timeView);

        // Measure the second row of content: [TITLE]
        measureChildWithMargins(titleView, availableWidth);
        totalHeight += titleView.getMeasuredHeight() + getVerticalMarginsFor(titleView);

        totalHeight += getPaddingBottom();

        int measuredHeight = getMeasuredHeightFor(heightMeasureSpec, totalHeight);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), measuredHeight);
    }

    private void measureAndUpdateFirstRowContent(int availableWidth) {
        measureChildWithMargins(timeView, availableWidth);
    }



    private void measureChildWithMargins(@NonNull View child, int availableWidth) {
        int maxChildWidth = availableWidth - getHorizontalMarginsFor(child);
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxChildWidth, MeasureSpec.AT_MOST);
        child.measure(widthMeasureSpec, UNSPECIFIED_MEASURE_SPEC);
    }

    private static int getHorizontalMarginsFor(@NonNull View view) {
        if (!(view.getLayoutParams() instanceof MarginLayoutParams)) {
            return 0;
        }
        MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
        return lp.leftMargin + lp.rightMargin;
    }

    private static int getVerticalMarginsFor(@NonNull View view) {
        if (!(view.getLayoutParams() instanceof MarginLayoutParams)) {
            return 0;
        }
        MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
        return lp.topMargin + lp.bottomMargin;
    }

    private static int getMeasuredHeightFor(int heightMeasureSpec, int totalHeight) {
        int measuredHeight;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
                break;
            case MeasureSpec.AT_MOST:
                measuredHeight = Math.min(MeasureSpec.getSize(heightMeasureSpec), totalHeight);
                break;
            default:
                measuredHeight = totalHeight;
                break;
        }
        return measuredHeight;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        boolean isLtr = isLayoutLtr();
        int clientLeft = left + getPaddingLeft();
        int clientTop = top + getPaddingTop();
        int clientRight = right - getPaddingRight();

        // Layout first row: [TIME] --- [TRACK LABEL]? - [TRACK DRAWABLE]?
        int firstRowBottom = obtainFristRowBottomAndLayout(clientLeft, clientTop, clientRight, isLtr);

        // Measure the second row of content: [TITLE]
        layoutSecondRow(clientLeft, clientRight, firstRowBottom);

        updateTrackLineBounds();
    }

    private int obtainFristRowBottomAndLayout(int clientLeft, int clientTop, int clientRight, boolean isLtr) {
        int timeLeft;
        int timeTop = clientTop + getTopMarginFor(timeView);
        int timeRight;
        int timeMeasuredWidth = timeView.getMeasuredWidth();
        int timeHeight = timeView.getMeasuredHeight();

        if (isLtr) {
            timeLeft = clientLeft + getLeftMarginFor(timeView);
            timeRight = Math.min(timeLeft + timeMeasuredWidth, clientRight - getRightMarginFor(timeView));
        } else {
            timeRight = clientRight + getRightMarginFor(timeView);
            timeLeft = Math.max(timeRight - timeMeasuredWidth, clientLeft + getLeftMarginFor(timeView));
        }

        timeView.layout(timeLeft, timeTop, timeRight, timeTop + timeHeight);

        return timeView.getBottom() + getBottomMarginFor(timeView);
    }

    private void layoutSecondRow(int clientLeft, int clientRight, int firstRowBottom) {
        int titleTop = firstRowBottom + getTopMarginFor(titleView);
        int titleHeight = titleView.getMeasuredHeight();
        titleView.layout(clientLeft, titleTop, clientRight, titleTop + titleHeight);
    }

    private int getLeftMarginFor(@NonNull View view) {
        if (!(view.getLayoutParams() instanceof MarginLayoutParams)) {
            return 0;
        }
        MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
        return lp.leftMargin;
    }

    private int getTopMarginFor(@NonNull View view) {
        if (!(view.getLayoutParams() instanceof MarginLayoutParams)) {
            return 0;
        }
        MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
        return lp.topMargin;
    }

    private int getRightMarginFor(@NonNull View view) {
        if (!(view.getLayoutParams() instanceof MarginLayoutParams)) {
            return 0;
        }
        MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
        return lp.rightMargin;
    }

    private int getBottomMarginFor(@NonNull View view) {
        if (!(view.getLayoutParams() instanceof MarginLayoutParams)) {
            return 0;
        }
        MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
        return lp.bottomMargin;
    }

    private boolean isLayoutLtr() {
        return getLayoutDirection() == LAYOUT_DIRECTION_LTR;
    }

    private void updateTrackLineBounds() {
        // The bounds are double the line height so that we can draw the bottom half of
        // the line outside of the clip path we define in onDraw(), to hide the bottom
        // rounded corners for the track line and pretend it only has them on the top edge
        trackLineBounds.set(0f, 0f, (float) getWidth(), trackLineHeightPx * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawTrackLineOn(canvas);
        super.onDraw(canvas);
    }

    private void drawTrackLineOn(Canvas canvas) {
        canvas.save();
        // This is to hide the bottom edge's rounded corners
        canvas.clipRect(0f, 0f, getWidth(), trackLineHeightPx, Region.Op.INTERSECT);
        canvas.drawRoundRect(trackLineBounds, trackLineCornerRadiusPx, trackLineCornerRadiusPx, trackBgPaint);
        canvas.restore();
    }

    @UiThread
    public void updateWith(Placeholder placeholder) {
        // TODO delegate to custom views
        updateTimeWith(placeholder.timeSlot());
        updateTitleWith(placeholder.name());
    }



    private void updateTimeWith(TimeSlot timeSlot) {
        String startTime = dateFormat.format(timeSlot.getStart());
        String endTime = dateFormat.format(timeSlot.getEnd());
        timeView.setText(String.format(TIMESLOT_TEMPLATE, startTime, endTime));
    }

    private void updateTitleWith(String talkName) {
        titleView.setText(talkName);
    }


}
