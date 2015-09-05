package io.kazak.notifications;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

import java.util.List;

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

    public void showNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            int notificationId = (int) (SINGLE_NOTIFICATION_ID + (System.currentTimeMillis() % 1000));
            showNotification(notification, notificationId);
        }
    }

    private void showNotification(Notification singleNotification, int notificationId) {
        notificationManagerCompat.notify(notificationId, singleNotification);
    }
}
