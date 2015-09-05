package io.kazak.notifications;

import android.app.IntentService;
import android.content.Intent;

public class EventAlarmService extends IntentService {

    public EventAlarmService() {
        super(EventAlarmService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO: get next sessions and display notifications
    }

}
