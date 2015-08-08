package io.kazak.schedule.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import io.kazak.R;
import io.kazak.model.Talk;

public class ScheduleAdapter extends BaseAdapter {

    private final List<Talk> talks;
    private final LayoutInflater inflater;

    public ScheduleAdapter(Context context, List<Talk> talks) {
        this.talks = talks;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return talks.size();
    }

    @Override
    public Object getItem(int position) {
        return talks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TalkView talkView = reuseOrInflateTalkView(convertView, parent);
        talkView.updateWith((Talk) getItem(position));
        return talkView;
    }

    private TalkView reuseOrInflateTalkView(View convertView, ViewGroup parent) {
        TalkView talkView;
        if (convertView instanceof TalkView) {
            talkView = (TalkView) convertView;
        } else {
            talkView = (TalkView) inflater.inflate(R.layout.view_talk_item, parent, false);
        }
        return talkView;
    }

}
