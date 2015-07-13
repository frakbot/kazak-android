package uk.co.droidcon.kazak.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import uk.co.droidcon.kazak.R;
import uk.co.droidcon.kazak.model.Talk;

public class TalkView extends CardView {

    private TextView titleView;

    public TalkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TalkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.merge_talk_card_contents, this);

        titleView = (TextView) findViewById(R.id.talk_title);
    }

    public void updateWith(Talk talk) {
        // TODO properly bind to data
        titleView.setText(talk.getName());
    }

}
