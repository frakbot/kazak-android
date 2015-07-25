package uk.co.droidcon.kazak.notifications;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

public class Notifier {

    private static final int SINGLE_NOTIFICATION_ID = 42;

    private final NotificationManagerCompat notificationManagerCompat;
    
    private Notifier(NotificationManagerCompat notificationManagerCompat) {
        this.notificationManagerCompat = notificationManagerCompat;
    }

    public static Notifier from(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        return new Notifier(notificationManagerCompat);
    }

    public void showNotification(Notification singleNotification) {
        notificationManagerCompat.notify(SINGLE_NOTIFICATION_ID, singleNotification);
    }
}
