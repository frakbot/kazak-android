package io.kazak.schedule.view.table;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.Date;

import io.kazak.model.Room;
import io.kazak.model.Talk;
import io.kazak.schedule.view.table.base.TableViewHolder;
import io.kazak.schedule.view.TalkCardView;

public class ScheduleTableViewHolder extends TableViewHolder<Talk, Room, Date> {

    private TalkCardView cardView;

    public ScheduleTableViewHolder(@NonNull TalkCardView cardView, @NonNull RecyclerView.Adapter adapter) {
        super(cardView, adapter);
        this.cardView = cardView;
    }

    @Override
    public void updateWith(Talk item, Room row, Date start, Date end, boolean isPlaceholder) {
        super.updateWith(item, row, start, end, isPlaceholder);
        cardView.updateWith(item);
    }

    TalkCardView getCardView() {
        return cardView;
    }
}
