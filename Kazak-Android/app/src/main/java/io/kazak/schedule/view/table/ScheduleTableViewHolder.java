package io.kazak.schedule.view.table;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import java.util.Date;

import io.kazak.BR;
import io.kazak.model.Event;
import io.kazak.model.Room;
import io.kazak.schedule.view.table.base.TableViewHolder;

public class ScheduleTableViewHolder extends TableViewHolder<Pair<Event, Room>, Room, Date> {

    @NonNull
    private final ViewDataBinding binding;

    public ScheduleTableViewHolder(
            @NonNull RecyclerView.Adapter adapter,
            @NonNull ViewDataBinding binding) {
        super(binding.getRoot(), adapter);
        this.binding = binding;
    }

    @Override
    public void updateWith(Pair<Event, Room> item, Room row, Date start, Date end, boolean isPlaceholder) {
        super.updateWith(item, row, start, end, isPlaceholder);
        updateWith(item.first);
    }

    public void updateWith(@NonNull Event event) {
        binding.setVariable(BR.event, event);
        binding.executePendingBindings();
    }

}
