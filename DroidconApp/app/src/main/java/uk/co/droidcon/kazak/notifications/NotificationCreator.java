package uk.co.droidcon.kazak.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import uk.co.droidcon.kazak.R;
import uk.co.droidcon.kazak.model.Talk;
import uk.co.droidcon.kazak.schedule.ScheduleActivity;

public class NotificationCreator {

    // pulsate every 1 second, indicating a relatively high degree of urgency
    private static final int NOTIFICATION_LED_ON_MS = 100;
    private static final int NOTIFICATION_LED_OFF_MS = 1000;
    private static final int NOTIFICATION_ARGB_COLOR = 0xff0088ff; // cyan

    private final Context context;

    public NotificationCreator(Context context) {
        this.context = context;
    }

    public Notification createFrom(Talk talk) {
        NotificationCompat.Builder summaryBuilder = createDefaultBuilder();
        summaryBuilder
                .setContentIntent(createPendingIntentForSingleSession(talk.getId()))
                .setContentTitle(talk.getName())
                .setContentText(talk.getRoom().getName());

        NotificationCompat.BigTextStyle richNotification = createBigTextRichNotification(summaryBuilder, talk);

        return richNotification.build();
    }

    private NotificationCompat.Builder createDefaultBuilder() {
        Resources resources = context.getResources();

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
        //extender.setBackground(BitmapFactory.decodeResource(resources, R.drawable.notification_background));

        return new NotificationCompat.Builder(context)
                //.setColor(resources.getColor(R.color.theme_primary))
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setLights(
                        NOTIFICATION_ARGB_COLOR,
                        NOTIFICATION_LED_ON_MS,
                        NOTIFICATION_LED_OFF_MS)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .extend(extender);
    }

    private PendingIntent createPendingIntentForSingleSession(String sessionId) {
        TaskStackBuilder taskBuilder = createBaseTaskStackBuilder();
//        TODO: add open the talk detail activity
//        taskBuilder.addNextIntent(new Intent(Intent.ACTION_VIEW,
//                ScheduleContract.Sessions.buildSessionUri(sessionId)));
        return taskBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private TaskStackBuilder createBaseTaskStackBuilder() {
        Intent baseIntent = new Intent(context, ScheduleActivity.class);
        baseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return TaskStackBuilder.create(context)
                .addNextIntent(baseIntent);
    }

    private NotificationCompat.BigTextStyle createBigTextRichNotification(
            NotificationCompat.Builder notifBuilder, Talk talk) {
        String speakers = talk.getSpeakersNames(); // TODO: get the actual ones from the session
        String roomName = talk.getRoom().getName();
        StringBuilder bigTextBuilder = new StringBuilder()
                .append(context.getString(R.string.session_notification_starting_by, speakers))
                .append('\n')
                .append(context.getString(R.string.session_notification_starting_in, roomName));
        return new NotificationCompat.BigTextStyle(
                notifBuilder)
                .setBigContentTitle(talk.getName())
                .bigText(bigTextBuilder.toString());
    }
}
