package io.kazak.schedule.view;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.List;

import io.kazak.BR;
import io.kazak.model.Id;

public class ScheduleBindingState extends BaseObservable {

    private ScheduleEventView.Listener listener;

    private List<? extends Id> favorites;

    @Bindable
    public ScheduleEventView.Listener getListener() {
        return listener;
    }

    public void setListener(ScheduleEventView.Listener listener) {
        this.listener = listener;
        notifyPropertyChanged(BR.listener);
    }

    @Bindable
    public List<? extends Id> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<? extends Id> favorites) {
        this.favorites = favorites;
        notifyPropertyChanged(BR.favorites);
    }

}
