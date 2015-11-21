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
import io.kazak.model.Ceremony;
import io.kazak.model.Talk;

public class CeremonyCardView extends CardView implements ScheduleEventView<Ceremony> {

    private CeremonyView ceremonyView;

    public CeremonyCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CeremonyCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.view_schedule_ceremony, this);
        ceremonyView = (CeremonyView) findViewById(R.id.schedule_card_content);
    }

    @Override
    public void updateWith(@NonNull final Ceremony ceremony, @Nullable final Listener listener) {
        ceremonyView.updateWith(ceremony);
        ceremonyView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTalkClicked(ceremony.id());
                }
            }
        });
    }

    public void setFavorite(boolean isFavorite) {
        //TODO show favorite state
    }

    @BindingAdapter({"bind:event", "bind:listener"})
    public static void bind(@NonNull CeremonyCardView talkCardView, @NonNull Ceremony ceremony, @Nullable Listener listener) {
        talkCardView.updateWith(ceremony, listener);
    }

}
