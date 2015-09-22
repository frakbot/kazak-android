package io.kazak.schedule.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
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
import io.kazak.model.Color;
import io.kazak.model.Id;
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
    private Drawable trackDrawable;

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

    public TalkView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr);

        dateFormat = new SimpleDateFormat(TIMESLOT_BOUND_PATTERN, Locale.UK);
        trackDrawableBounds = new Rect();
        trackLineBounds = new RectF();
        trackBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TalkView, defStyleAttr, defStyleRes);
        trackDrawablePaddingPx = a.getDimensionPixelSize(R.styleable.TalkView_android_drawablePadding, 0);
        trackDrawableSizePx = a.getDimensionPixelSize(R.styleable.TalkView_trackSymbolSize, 0);
        trackLineHeightPx = a.getDimensionPixelSize(R.styleable.TalkView_trackLineHeight, 0);
        trackLineCornerRadiusPx = a.getDimensionPixelSize(R.styleable.TalkView_trackLineCornerRadius, 0);
        drawableOpticalBalanceOffsetPx = a.getDimensionPixelSize(R.styleable.TalkView_drawableOpticalBalanceOffset, 0);
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
        int totalHeight = getPaddingTop() + Math.max(
                timeView.getMeasuredHeight() + getVerticalMarginsFor(timeView),
                trackView.getMeasuredHeight() + getVerticalMarginsFor(trackView)
        );

        // Measure the second row of content: [TITLE]
        measureChildWithMargins(titleView, availableWidth);
        totalHeight += titleView.getMeasuredHeight() + getVerticalMarginsFor(titleView);

        // Measure the third row of content: [SPEAKERS] - [FAVORITE] (assuming not wrapping favorite to fourth row)
        measureAndUpdateThirdRowContent(availableWidth);
        totalHeight += speakersView.getMeasuredHeight() + getVerticalMarginsFor(speakersView);

        totalHeight += getPaddingBottom();

        int measuredHeight = getMeasuredHeightFor(heightMeasureSpec, totalHeight);
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
        int clientBottom = bottom - getPaddingBottom();

        // Layout first row: [TIME] --- [TRACK LABEL]? - [TRACK DRAWABLE]?
        int firstRowBottom = layoutFirstRow(clientLeft, clientTop, clientRight, isLtr);

        // Measure the second row of content: [TITLE]
        int secondRowBottom = layoutSecondRow(clientLeft, clientRight, firstRowBottom);

        // Measure the third row of content: [SPEAKERS] - [FAVORITE] (assuming not wrapping favorite to fourth row)
        layoutThirdRow(clientLeft, clientRight, clientBottom, secondRowBottom, isLtr);

        updateTrackLineBounds();
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

        if (showTrackDrawable) {
            drawTrackDrawableOn(canvas);
        }
    }

    private void drawTrackLineOn(Canvas canvas) {
        canvas.save();
        // This is to hide the bottom edge's rounded corners
        canvas.clipRect(0f, 0f, getWidth(), trackLineHeightPx, Region.Op.INTERSECT);
        canvas.drawRoundRect(trackLineBounds, trackLineCornerRadiusPx, trackLineCornerRadiusPx, trackBgPaint);
        canvas.restore();
    }

    private void drawTrackDrawableOn(Canvas canvas) {
        canvas.drawCircle(trackDrawableBounds.centerX(), trackDrawableBounds.centerY(), trackDrawableBounds.width() / 2f, trackBgPaint);
        if (trackDrawable != null) {
            int saveState = canvas.save();

            canvas.translate(
                    trackDrawableBounds.centerX() - trackDrawableSizePx / 2,
                    trackDrawableBounds.centerY() - trackDrawableSizePx / 2);
            trackDrawable.draw(canvas);

            canvas.restoreToCount(saveState);
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
        Color color = track.color();
        if (color != null) {
            int trackColor = color.getIntValue();
            trackView.setTextColor(trackColor);
            trackBgPaint.setColor(trackColor);
        }
        trackDrawable = getTrackDrawableFor(track.id());
        invalidate(trackDrawableBounds);
        invalidate(toRect(trackLineBounds));
    }

    private Drawable getTrackDrawableFor(Id id) {
        return null;        // TODO map to the icons! (possibly using an enum or something)
    }

    private static Rect toRect(RectF rectF) {
        return new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
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
