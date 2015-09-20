package io.kazak.schedule.view;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import io.kazak.R;
import io.kazak.model.Talk;

public class TalkCardView extends CardView implements ScheduleEventView<Talk> {

    private TalkView talkView;

    public TalkCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TalkCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.view_schedule_talk, this);
        talkView = (TalkView) findViewById(R.id.schedule_card_content);
    }

    @Override
    public void updateWith(@NonNull final Talk talk, @Nullable final Listener listener) {
        talkView.updateWith(talk);
        talkView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTalkClicked(talk.id());
                }
            }
        });
    }

    public void setFavorite(boolean isFavorite) {
        //TODO show favorite state
    }

    @BindingAdapter({"bind:event", "bind:listener"})
    public static void bind(@NonNull TalkCardView talkCardView, @NonNull Talk talk, @Nullable ScheduleEventView.Listener listener) {
        talkCardView.updateWith(talk, listener);
    }

}
