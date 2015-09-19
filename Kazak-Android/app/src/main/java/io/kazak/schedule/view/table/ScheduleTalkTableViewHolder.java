package io.kazak.schedule.view.table;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import java.util.Date;

import io.kazak.model.Room;
import io.kazak.model.Talk;
import io.kazak.schedule.view.ScheduleEventView;
import io.kazak.schedule.view.table.base.TableViewHolder;
import io.kazak.schedule.view.TalkCardView;

public class ScheduleTalkTableViewHolder extends TableViewHolder<Pair<Talk, Room>, Room, Date> {

    private TalkCardView cardView;

    @Nullable
    private ScheduleEventView.Listener listener;

    public ScheduleTalkTableViewHolder(
            @NonNull TalkCardView cardView,
            @NonNull RecyclerView.Adapter adapter,
            @Nullable ScheduleEventView.Listener listener) {
        super(cardView, adapter);
        this.cardView = cardView;
        this.listener = listener;
    }

    @Override
    public void updateWith(Pair<Talk, Room> item, Room row, Date start, Date end, boolean isPlaceholder) {
        super.updateWith(item, row, start, end, isPlaceholder);
        cardView.updateWith(item.first, listener);
    }

    TalkCardView getCardView() {
        return cardView;
    }

}
