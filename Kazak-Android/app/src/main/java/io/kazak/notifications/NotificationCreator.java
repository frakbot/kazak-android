package io.kazak.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.ArrayList;
import java.util.List;

import io.kazak.R;
import io.kazak.model.Talk;
import io.kazak.schedule.ScheduleActivity;
import io.kazak.talk.TalkDetailsActivity;

public class NotificationCreator {

    private static final String GROUP_KEY_NOTIFY_SESSION = "group_key_notify_session";
    private static final int STACKED_NOTIFICATIONS_ID_OFFSET = 1;

    // pulsate every 1 second, indicating a relatively high degree of urgency
    private static final int NOTIFICATION_LED_ON_MS = 100;
    private static final int NOTIFICATION_LED_OFF_MS = 1000;

    private final Context context;

    public NotificationCreator(Context context) {
        this.context = context;
    }

    public List<Notification> createFrom(List<Talk> talks) {
        List<Notification> notifications = new ArrayList<>();
        for (Talk talk : talks) {
            notifications.add(createFrom(talk));
        }
        if (talks.size() > 1) {
            notifications.add(createSummaryNotification(talks));
        }
        return notifications;
    }

    private Notification createFrom(Talk talk) {
        NotificationCompat.Builder notificationBuilder = createDefaultBuilder(1);
        notificationBuilder
                .setContentIntent(createPendingIntentForSingleSession(talk.getId()))
                .setContentTitle(talk.getName())
                .setContentText(talk.getRoom().getName())
                .setGroup(GROUP_KEY_NOTIFY_SESSION);

        NotificationCompat.BigTextStyle richNotification = createBigTextRichNotification(notificationBuilder, talk);

        return richNotification.build();
    }

    private Notification createSummaryNotification(List<Talk> talks) {
        NotificationCompat.Builder summaryBuilder = createDefaultBuilder(talks.size());
        summaryBuilder
                .setContentIntent(createPendingIntentForMultipleSessions())
                .setContentTitle(createSummaryTitle(talks.size()))
                .setGroup(GROUP_KEY_NOTIFY_SESSION)
                .setGroupSummary(true)
                .setLocalOnly(true);

        NotificationCompat.InboxStyle richNotification = createInboxStyleRichNotification(summaryBuilder, talks);

        return richNotification.build();
    }

    private NotificationCompat.Builder createDefaultBuilder(int talksCount) {
        Resources resources = context.getResources();

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
        // TODO: define a default background for wear notifications
        //extender.setBackground(BitmapFactory.decodeResource(resources, R.drawable.notification_background));

        return new NotificationCompat.Builder(context)
                // TODO: use topic color
                //.setColor(resources.getColor(R.color.theme_primary))
                .setTicker(
                        context.getResources().getQuantityString(
                                R.plurals.session_notification_ticker,
                                talksCount,
                                talksCount
                        )
                )
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setLights(
                        resources.getColor(R.color.notification_led_color),
                        NOTIFICATION_LED_ON_MS,
                        NOTIFICATION_LED_OFF_MS
                )
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .extend(extender);
    }

    private PendingIntent createPendingIntentForSingleSession(String talkId) {
        TaskStackBuilder taskBuilder = createBaseTaskStackBuilder();
        Intent talkDetailIntent = new Intent(context, TalkDetailsActivity.class);
        talkDetailIntent.putExtra(TalkDetailsActivity.EXTRA_TALK_ID, talkId);
        taskBuilder.addNextIntent(talkDetailIntent);

        return taskBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent createPendingIntentForMultipleSessions() {
        TaskStackBuilder taskBuilder = createBaseTaskStackBuilder();
        return taskBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private TaskStackBuilder createBaseTaskStackBuilder() {
        Intent baseIntent = new Intent(context, ScheduleActivity.class);
        baseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return TaskStackBuilder.create(context)
                .addNextIntent(baseIntent);
    }

    private NotificationCompat.BigTextStyle createBigTextRichNotification(
            NotificationCompat.Builder notificationBuilder, Talk talk) {
        String speakers = talk.getSpeakersNames();
        String roomName = talk.getRoom().getName();
        StringBuilder bigTextBuilder = new StringBuilder()
                .append(context.getString(R.string.session_notification_starting_by, speakers))
                .append('\n')
                .append(context.getString(R.string.session_notification_starting_in, roomName));
        return new NotificationCompat.BigTextStyle(notificationBuilder)
                .setBigContentTitle(talk.getName())
                .bigText(bigTextBuilder.toString());
    }

    private NotificationCompat.InboxStyle createInboxStyleRichNotification(
            NotificationCompat.Builder notificationBuilder, List<Talk> talks) {
        String bigContentTitle = createSummaryTitle(talks.size());
        NotificationCompat.InboxStyle richNotification = new NotificationCompat.InboxStyle(notificationBuilder)
                .setBigContentTitle(bigContentTitle);
        for (Talk talk : talks) {
            richNotification.addLine(
                    context.getString(
                            R.string.room_session_notification,
                            talk.getRoom().getName(),
                            talk.getName()
                    )
            );
        }

        return richNotification;
    }

    private String createSummaryTitle(int talksCount) {
        return context.getString(R.string.session_notification_count_starting, talksCount);
    }
}
