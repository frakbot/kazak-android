package io.kazak.schedule.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
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

    private final DateFormat dateFormat;

    private final int trackDrawablePaddingPx;
    private final int trackDrawableSizePx;
    private final int drawableOpticalBalanceOffsetPx;

    private TextView timeView;
    private TextView trackView;
    private TextView titleView;
    private TextView speakersView;
    private ImageButton favoriteView;

    private boolean showTrackDrawable = true;

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

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TalkView, defStyleAttr, defStyleRes);
        trackDrawablePaddingPx = a.getDimensionPixelSize(R.styleable.TalkView_android_drawablePadding, 0);
        trackDrawableSizePx = a.getDimensionPixelSize(R.styleable.TalkView_talkSymbolSize, 0);
        drawableOpticalBalanceOffsetPx = a.getDimensionPixelSize(R.styleable.TalkView_drawableOpticalBalanceOffset, 0);
        a.recycle();
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Fail fast, and optimize (EXACTLY, EXACTLY) scenario
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            throw new DeveloperError("This view only supports EXACTLY for the widthMeasureSpec");
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
            return;
        }

        int availableWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingStart() - getPaddingEnd();

        // Measure the first row of content: [TIME] --- [TRACK LABEL]? - [TRACK DRAWABLE]?
        measureAndUpdateFirstRowContent(widthMeasureSpec, heightMeasureSpec, availableWidth);
        int totalHeight = getPaddingTop() + timeView.getMaxHeight() + getVerticalMarginsFor(trackView);

        // Measure the second row of content: [TITLE]
        measureChildWithMargins(titleView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        totalHeight += titleView.getMaxHeight() + getVerticalMarginsFor(titleView);

        // Measure the third row of content: [SPEAKERS] - [FAVORITE] (assuming not wrapping favorite to fourth row)
        measureAndUpdateThirdRowContent(widthMeasureSpec, heightMeasureSpec, availableWidth);
        totalHeight += speakersView.getMaxHeight() + getVerticalMarginsFor(speakersView);

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), totalHeight + getPaddingBottom());
    }

    private void measureAndUpdateFirstRowContent(int widthMeasureSpec, int heightMeasureSpec, int availableWidth) {
        int usedWidth = 0;
        measureChildWithMargins(timeView, widthMeasureSpec, usedWidth, heightMeasureSpec, 0);

        int totalTimeWidth = timeView.getMeasuredWidth() + getHorizontalMarginsFor(timeView);
        usedWidth = totalTimeWidth;
        measureChildWithMargins(trackView, widthMeasureSpec, usedWidth, heightMeasureSpec, 0);
        int totalTrackLabelWidth = trackView.getMeasuredWidth() - trackDrawablePaddingPx - getHorizontalMarginsFor(trackView);

        int maxTimeWidthWithoutTrackLabel = availableWidth - trackDrawableSizePx + drawableOpticalBalanceOffsetPx;
        int maxTimeWidthWithTrackLabel = maxTimeWidthWithoutTrackLabel - totalTrackLabelWidth;
        updateTrackLabelAndDrawableVisibility(totalTimeWidth, maxTimeWidthWithoutTrackLabel, maxTimeWidthWithTrackLabel);
    }

    private void updateTrackLabelAndDrawableVisibility(int totalTimeWidth, int maxTimeViewWidthWithoutTrackLabel, int maxTimeViewWidthWithTrackLabel) {
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

    private void measureAndUpdateThirdRowContent(int widthMeasureSpec, int heightMeasureSpec, int availableWidth) {
        int usedWidth = 0;
        measureChildWithMargins(favoriteView, widthMeasureSpec, usedWidth, heightMeasureSpec, 0);

        usedWidth = favoriteView.getMeasuredWidth() + getHorizontalMarginsFor(favoriteView);

        measureChildWithMargins(speakersView, widthMeasureSpec, usedWidth, heightMeasureSpec, 0);
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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO measure & layout stuff
    }

    public void updateWith(Talk talk) {
        // TODO delegate to custom views
        updateTimeWith(talk.timeSlot());
        updateTrackWith(talk.track());
        updateTitleWith(talk.name());
        updateSpeakersWith(talk.speakersNames());
        updateFavoriteWith(false);
    }

    private void updateTrackWith(@NonNull Track track) {
        trackView.setText(track.name().toUpperCase(Locale.getDefault()));
        trackView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.temp_circle_icon, 0, 0, 0);
        setBackgroundTintCompat(trackView, track.color());
    }

    private static void setBackgroundTintCompat(View view, @ColorInt int color) {
        Drawable background = view.getBackground();
        if (background != null) {
            background.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
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
