package io.kazak.schedule.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.kazak.R;
import io.kazak.model.Talk;
import io.kazak.model.TimeSlot;
import io.kazak.model.Track;

public class TalkCardView extends CardView {

    private static final String TIMESLOT_BOUND_PATTERN = "HH:mm";
    private static final String TIMESLOT_TEMPLATE = "%1$s—%2$s";

    private final DateFormat dateFormat;

    private TextView trackView;
    private TextView timeView;
    private TextView titleView;
    private TextView speakersView;
    private ImageButton favoriteView;

    public TalkCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("SimpleDateFormat")       // At this time we want to always use 24h-format
    public TalkCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        dateFormat = new SimpleDateFormat(TIMESLOT_BOUND_PATTERN);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.merge_schedule_talk_card, this);

        trackView = (TextView) findViewById(R.id.track_label);
        timeView = (TextView) findViewById(R.id.session_time);
        titleView = (TextView) findViewById(R.id.session_title);
        speakersView = (TextView) findViewById(R.id.session_speakers);
        favoriteView = (ImageButton) findViewById(R.id.session_favorite);
    }

    public void updateWith(@NonNull Talk talk) {
        // TODO properly bind to data
        updateTrackWith(talk.track());
        updateTimeWith(talk.timeSlot());
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
