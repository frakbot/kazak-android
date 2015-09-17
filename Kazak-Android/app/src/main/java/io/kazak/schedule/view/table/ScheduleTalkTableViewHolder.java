package io.kazak.schedule.view.table;

import android.support.annotation.NonNull;
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

    public ScheduleTalkTableViewHolder(@NonNull TalkCardView cardView, @NonNull RecyclerView.Adapter adapter) {
        super(cardView, adapter);
        this.cardView = cardView;
    }

    @Override
    public void updateWith(Pair<Talk, Room> item, Room row, Date start, Date end, boolean isPlaceholder, ScheduleEventView.Listener listener) {
        super.updateWith(item, row, start, end, isPlaceholder, listener);
        cardView.updateWith(item.first, listener);
    }

    TalkCardView getCardView() {
        return cardView;
    }

}
