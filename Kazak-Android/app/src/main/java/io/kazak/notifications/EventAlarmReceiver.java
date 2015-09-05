package io.kazak.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EventAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, EventAlarmService.class);
        context.startService(serviceIntent);
    }

}
