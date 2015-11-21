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
import io.kazak.model.Placeholder;

public class PlaceHolderCardView extends CardView implements ScheduleEventView<Placeholder> {

    private PlaceHolderView placeHolderView;

    public PlaceHolderCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaceHolderCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.view_schedule_placeholder, this);
        placeHolderView = (PlaceHolderView) findViewById(R.id.schedule_card_content);
    }

    @Override
    public void updateWith(@NonNull final Placeholder placeholder, @Nullable final Listener listener) {
        placeHolderView.updateWith(placeholder);
        placeHolderView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTalkClicked(placeholder.id());
                }
            }
        });
    }


    @BindingAdapter({"bind:event", "bind:listener"})
    public static void bind(@NonNull PlaceHolderCardView placeHolderCardView, @NonNull Placeholder placeholder, @Nullable Listener listener) {
        placeHolderCardView.updateWith(placeholder, listener);
    }

}
