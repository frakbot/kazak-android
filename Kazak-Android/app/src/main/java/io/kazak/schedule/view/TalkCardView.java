package io.kazak.schedule.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;

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
    public void updateWith(@NonNull Talk talk) {
        talkView.updateWith(talk);
    }

}
