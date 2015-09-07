package io.kazak.schedule.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.kazak.R;
import io.kazak.base.DeveloperError;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import io.kazak.model.Track;

public class TalkView extends ViewGroup {

    private static final String TIMESLOT_BOUND_PATTERN = "HH:mm";
    private static final String TIMESLOT_TEMPLATE = "%1$s—%2$s";
    private static final int UNSPECIFIED_MEASURE_SPEC = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

    private final DateFormat dateFormat;

    private final int trackDrawablePaddingPx;
    private final int trackDrawableSizePx;
    private final int drawableOpticalBalanceOffsetPx;
    private final Rect trackDrawableBounds;
    private final Paint trackBgPaint;
    private final RectF trackLineBounds;
    private final int trackLineHeightPx;
    private final int trackLineCornerRadiusPx;

    private boolean showTrackDrawable = true;

    private TextView timeView;
    private TextView trackView;
    private TextView titleView;
    private TextView speakersView;
    private ImageButton favoriteView;

    public TalkView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TalkView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.TalkViewDefaultStyle);
    }

    @SuppressLint("SimpleDateFormat")       // At this time we want to always use 24h-format
    public TalkView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr);
        dateFormat = new SimpleDateFormat(TIMESLOT_BOUND_PATTERN);
        trackDrawableBounds = new Rect();
        trackLineBounds = new RectF();
        trackBgPaint = createTrackBgPaint();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TalkView, defStyleAttr, defStyleRes);
        trackDrawablePaddingPx = a.getDimensionPixelSize(R.styleable.TalkView_android_drawablePadding, 0);
        trackDrawableSizePx = a.getDimensionPixelSize(R.styleable.TalkView_trackSymbolSize, 0);
        trackLineHeightPx = a.getDimensionPixelSize(R.styleable.TalkView_trackLineHeight, 0);
        trackLineCornerRadiusPx = a.getDimensionPixelSize(R.styleable.TalkView_trackLineCornerRadius, 0);
        drawableOpticalBalanceOffsetPx = a.getDimensionPixelSize(R.styleable.TalkView_drawableOpticalBalanceOffset, 0);
        a.recycle();

        super.setWillNotDraw(false);
    }

    @Override
    public void setWillNotDraw(boolean willNotDraw) {
        throw new DeveloperError("Nein nein nein nein nein! ಠ_ಠ");
    }

    @NonNull
    private Paint createTrackBgPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.merge_schedule_talk_view_contents, this);

        timeView = (TextView) findViewById(R.id.session_time);
        trackView = (TextView) findViewById(R.id.track_label);
        titleView = (TextView) findViewById(R.id.session_title);
        speakersView = (TextView) findViewById(R.id.session_speakers);
        favoriteView = (ImageButton) findViewById(R.id.session_favorite);
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

        // Measure the third row of content: [SPEAKERS] - [FAVORITE] (assuming not wrapping favorite to fourth row)
        measureAndUpdateThirdRowContent(availableWidth);
        totalHeight += speakersView.getMeasuredHeight() + getVerticalMarginsFor(speakersView);

        totalHeight += getPaddingBottom();

        int measuredHeight;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            measuredHeight = Math.min(MeasureSpec.getSize(heightMeasureSpec), totalHeight);
        } else {
            measuredHeight = totalHeight;
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), measuredHeight);
    }

    private void measureAndUpdateFirstRowContent(int availableWidth) {
        measureChildWithMargins(timeView, availableWidth);
        int totalTimeWidth = timeView.getMeasuredWidth() + getHorizontalMarginsFor(timeView);

        measureChildWithMargins(trackView, Integer.MAX_VALUE);
        int totalTrackLabelWidth = trackView.getMeasuredWidth() + trackDrawablePaddingPx + getHorizontalMarginsFor(trackView);

        int maxTimeWidthWithoutTrackLabel = availableWidth - trackDrawableSizePx + drawableOpticalBalanceOffsetPx;
        int maxTimeWidthWithTrackLabel = maxTimeWidthWithoutTrackLabel - totalTrackLabelWidth;
        updateTrackLabelAndDrawableVisibility(totalTimeWidth, maxTimeWidthWithoutTrackLabel, maxTimeWidthWithTrackLabel);
    }

    private void updateTrackLabelAndDrawableVisibility(int totalTimeWidth,
                                                       int maxTimeViewWidthWithoutTrackLabel,
                                                       int maxTimeViewWidthWithTrackLabel) {
        if (totalTimeWidth <= maxTimeViewWidthWithTrackLabel) {
            // We can have the track drawable AND the track label
            trackView.setVisibility(VISIBLE);
            showTrackDrawable = true;
        } else if (totalTimeWidth <= maxTimeViewWidthWithoutTrackLabel) {
            // We can only have the track drawable
            trackView.setVisibility(GONE);
            showTrackDrawable = true;
        } else {
            // We can only have the time
            trackView.setVisibility(GONE);
            showTrackDrawable = false;
        }
    }

    private void measureAndUpdateThirdRowContent(int availableWidth) {
        int leftoverWidth = availableWidth;
        measureChildWithMargins(favoriteView, leftoverWidth);

        leftoverWidth -= favoriteView.getMeasuredWidth() + getHorizontalMarginsFor(favoriteView);
        leftoverWidth += drawableOpticalBalanceOffsetPx;

        measureChildWithMargins(speakersView, leftoverWidth);
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        boolean isLtr = isLayoutLtr();
        int clientLeft = left + getPaddingLeft();
        int clientTop = top + getPaddingTop();
        int clientRight = right - getPaddingRight();
        int clientBottom = bottom - getPaddingBottom();

        // Layout first row: [TIME] --- [TRACK LABEL]? - [TRACK DRAWABLE]?
        int firstRowBottom = layoutFirstRow(clientLeft, clientTop, clientRight, isLtr);

        // Measure the second row of content: [TITLE]
        int secondRowBottom = layoutSecondRow(clientLeft, clientRight, firstRowBottom);

        // Measure the third row of content: [SPEAKERS] - [FAVORITE] (assuming not wrapping favorite to fourth row)
        layoutThirdRow(clientLeft, clientRight, clientBottom, secondRowBottom, isLtr);

        // Update the track line bounds
        trackLineBounds.set(0f, 0f, (float) getWidth(), trackLineHeightPx * 2);
    }

    private int layoutFirstRow(int clientLeft, int clientTop, int clientRight, boolean isLtr) {
        int timeLeft;
        int timeTop = clientTop + getTopMarginFor(timeView);
        int timeRight;
        int timeMeasuredWidth = timeView.getMeasuredWidth();
        int timeHeight = timeView.getMeasuredHeight();

        int trackLeft = 0;
        int trackWidth = trackView.getMeasuredWidth();

        int trackDrawableLeft = 0;

        if (isLtr) {
            // [TIME]---[TRACK LABEL]?-[TRACK DRAWABLE]?
            int lastLeft = clientRight;
            if (showTrackDrawable) {
                trackDrawableLeft = lastLeft + drawableOpticalBalanceOffsetPx - trackDrawableSizePx;
                lastLeft = trackDrawableLeft - trackDrawablePaddingPx;

                if (isVisible(trackView)) {
                    trackLeft = lastLeft - getRightMarginFor(trackView) - trackWidth;
                    lastLeft = trackLeft - getLeftMarginFor(trackView);
                }
            }
            timeLeft = clientLeft + getLeftMarginFor(timeView);
            timeRight = Math.min(timeLeft + timeMeasuredWidth, lastLeft - getRightMarginFor(timeView));
        } else {
            // [TRACK DRAWABLE]?-[TRACK LABEL]?---[TIME]
            int lastRight = clientLeft;
            if (showTrackDrawable) {
                trackDrawableLeft = lastRight - drawableOpticalBalanceOffsetPx;
                lastRight = trackDrawableLeft + trackDrawableSizePx + trackDrawablePaddingPx;

                if (isVisible(trackView)) {
                    trackLeft = lastRight + getLeftMarginFor(trackView);
                    lastRight = trackLeft + trackWidth + getLeftMarginFor(trackView);
                }
            }
            timeRight = clientRight + getRightMarginFor(timeView);
            timeLeft = Math.max(timeRight - timeMeasuredWidth, lastRight + getLeftMarginFor(timeView));
        }

        timeView.layout(timeLeft, timeTop, timeRight, timeTop + timeHeight);

        if (showTrackDrawable) {
            int drawableTop = clientTop - drawableOpticalBalanceOffsetPx;
            trackDrawableBounds.set(
                    trackDrawableLeft,
                    drawableTop,
                    trackDrawableLeft + trackDrawableSizePx,
                    drawableTop + trackDrawableSizePx
            );

            if (isVisible(trackView)) {
                int trackTop = clientTop + getTopMarginFor(trackView);
                int trackHeight = trackView.getMeasuredHeight();
                trackView.layout(trackLeft, trackTop, trackLeft + trackWidth, trackTop + trackHeight);
            }
        }
        return timeView.getBottom() + getBottomMarginFor(timeView);
    }

    private static boolean isVisible(View view) {
        return view.getVisibility() != GONE;
    }

    private int layoutSecondRow(int clientLeft, int clientRight, int firstRowBottom) {
        int titleTop = firstRowBottom + getTopMarginFor(titleView);
        int titleHeight = titleView.getMeasuredHeight();
        titleView.layout(clientLeft, titleTop, clientRight, titleTop + titleHeight);
        return titleView.getBottom() + getBottomMarginFor(titleView);
    }

    private void layoutThirdRow(int clientLeft, int clientRight, int clientBottom, int secondRowBottom, boolean isLtr) {
        int speakersLeft;
        int speakersWidth = speakersView.getMeasuredWidth();
        int speakersHeight = speakersView.getMeasuredHeight();
        int speakersTop = secondRowBottom + getTopMarginFor(speakersView);

        int favoriteLeft;
        int favoriteWidth = favoriteView.getMeasuredWidth();
        int favoriteHeight = favoriteView.getMeasuredHeight();
        int favoriteTop = clientBottom - favoriteHeight - getBottomMarginFor(favoriteView);

        if (isLtr) {
            // [SPEAKERS] - [FAVORITE]
            speakersLeft = clientLeft + getLeftMarginFor(speakersView);
            favoriteLeft = clientRight + drawableOpticalBalanceOffsetPx - getRightMarginFor(favoriteView) - favoriteWidth;
        } else {
            // [FAVORITE] - [SPEAKERS]  
            speakersLeft = clientRight - speakersWidth + getRightMarginFor(speakersView);
            favoriteLeft = clientLeft - drawableOpticalBalanceOffsetPx + getLeftMarginFor(favoriteView);
        }
        speakersView.layout(speakersLeft, speakersTop, speakersLeft + speakersWidth, speakersTop + speakersHeight);
        favoriteView.layout(favoriteLeft, favoriteTop, favoriteLeft + favoriteWidth, favoriteTop + favoriteHeight);
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

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(0f, 0f, getWidth(), trackLineHeightPx, Region.Op.INTERSECT);
        canvas.drawRoundRect(trackLineBounds, trackLineCornerRadiusPx, trackLineCornerRadiusPx, trackBgPaint);
        canvas.restore();

        super.onDraw(canvas);

        if (showTrackDrawable) {
            canvas.drawCircle(trackDrawableBounds.centerX(), trackDrawableBounds.centerY(), trackDrawableBounds.width() / 2f, trackBgPaint);
            // TODO draw track drawable
        }
    }

    @UiThread
    public void updateWith(Talk talk) {
        // TODO delegate to custom views
        updateTimeWith(talk.timeSlot());
        updateTrackWith(talk.track());
        updateTitleWith(talk.name());
        updateSpeakersWith(talk.speakersNames());
        updateFavoriteWith(false);

        // TODO set maxLines for title and speakers
    }

    private void updateTrackWith(@NonNull Track track) {
        trackView.setText(track.name().toUpperCase(Locale.getDefault()));
        trackView.setTextColor(track.color());
        trackBgPaint.setColor(track.color());
    }

    private void updateTimeWith(TimeSlot timeSlot) {
        String startTime = dateFormat.format(timeSlot.getStart());
        String endTime = dateFormat.format(timeSlot.getEnd());
        timeView.setText(String.format(TIMESLOT_TEMPLATE, startTime, endTime));
    }

    private void updateTitleWith(String talkName) {
        titleView.setText(talkName);
    }

    private void updateSpeakersWith(String speakersNames) {
        speakersView.setText(speakersNames);
    }

    private void updateFavoriteWith(boolean favorite) {
        if (favorite) {
            favoriteView.setImageResource(R.drawable.ic_star_filled_20dp);
            favoriteView.setContentDescription(getResources().getString(R.string.description_remove_favorite_session));
        } else {
            favoriteView.setImageResource(R.drawable.ic_star_empty_20dp);
            favoriteView.setContentDescription(getResources().getString(R.string.description_favorite_session));
        }
    }

}
