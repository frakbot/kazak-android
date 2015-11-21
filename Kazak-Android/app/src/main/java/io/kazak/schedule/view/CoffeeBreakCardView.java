package io.kazak.schedule.view;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import io.kazak.R;
import io.kazak.model.CoffeeBreak;

public class CoffeeBreakCardView
        extends CardView implements ScheduleEventView<CoffeeBreak> {

    private CoffeeBreakView coffeeBreakView;

    public CoffeeBreakCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoffeeBreakCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.view_schedule_coffee_break, this);
        coffeeBreakView = (CoffeeBreakView) findViewById(R.id.schedule_card_content);
    }

    @Override
    public void updateWith(@NonNull final CoffeeBreak coffeeBreak, @Nullable final Listener listener) {
        coffeeBreakView.updateWith(coffeeBreak);
    }

    @BindingAdapter({"bind:event", "bind:listener"})
    public static void bind(@NonNull CoffeeBreakCardView coffeeBreakCardView, @NonNull CoffeeBreak coffeeBreak, @Nullable Listener listener) {
        coffeeBreakCardView.updateWith(coffeeBreak, listener);
    }

}
