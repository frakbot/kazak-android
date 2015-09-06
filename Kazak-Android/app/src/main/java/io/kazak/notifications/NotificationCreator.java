package io.kazak.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.ArrayList;
import java.util.List;

import io.kazak.R;
import io.kazak.model.Id;
import io.kazak.model.Room;
import io.kazak.model.Session;
import io.kazak.model.Talk;
import io.kazak.schedule.ScheduleActivity;
import io.kazak.talk.TalkDetailsActivity;

public class NotificationCreator {

    private static final String GROUP_KEY_NOTIFY_SESSION = "group_key_notify_session";

    // pulsate every 1 second, indicating a relatively high degree of urgency
    private static final int NOTIFICATION_LED_ON_MS = 100;
    private static final int NOTIFICATION_LED_OFF_MS = 1000;

    private final Context context;

    public NotificationCreator(Context context) {
        this.context = context;
    }

    public List<Notification> createFrom(List<? extends Session> sessions) {
        List<Notification> notifications = new ArrayList<>();
        for (Session session : sessions) {
            notifications.add(createFrom(session));
        }
        if (sessions.size() > 1) {
            notifications.add(createSummaryNotification(sessions));
        }
        return notifications;
    }

    private Notification createFrom(Session session) {
        NotificationCompat.Builder notificationBuilder = createDefaultBuilder(1);
        notificationBuilder
                .setContentIntent(createPendingIntentForSingleSession(session.id()))
                .setContentTitle(session.name())
                .setContentText(getDisplayedRooms(session))
                .setGroup(GROUP_KEY_NOTIFY_SESSION);

        setTrackColor(notificationBuilder, session);

        NotificationCompat.BigTextStyle richNotification = createBigTextRichNotification(notificationBuilder, session);

        return richNotification.build();
    }

    private void setTrackColor(NotificationCompat.Builder notificationBuilder, Session session) {
        if (!(session instanceof Talk)) {
            return;
        }
        Talk talk = (Talk) session;
        if (talk.track() != null && talk.track().color() != null) {
            notificationBuilder.setColor(talk.track().color().getIntValue());
        }
    }

    private String getDisplayedRooms(Session session) {
        List<Room> rooms = session.rooms();
        StringBuilder sb = new StringBuilder();
        for (Room room : rooms) {
            sb.append(room.getName()).append(", ");
        }
        if (sb.length() > 2) {
            sb.delete(sb.length() - 2, sb.length() - 1);
        }

        return rooms.get(0).getName();
    }

    private Notification createSummaryNotification(List<? extends Session> sessions) {
        NotificationCompat.Builder summaryBuilder = createDefaultBuilder(sessions.size());
        summaryBuilder
                .setContentIntent(createPendingIntentForMultipleSessions())
                .setContentTitle(createSummaryTitle(sessions.size()))
                .setGroup(GROUP_KEY_NOTIFY_SESSION)
                .setGroupSummary(true)
                .setLocalOnly(true);

        NotificationCompat.InboxStyle richNotification = createInboxStyleRichNotification(summaryBuilder, sessions);

        return richNotification.build();
    }

    private NotificationCompat.Builder createDefaultBuilder(int talksCount) {
        Resources resources = context.getResources();

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
        extender.setBackground(BitmapFactory.decodeResource(resources, R.drawable.notification_background));

        return new NotificationCompat.Builder(context)
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

    private PendingIntent createPendingIntentForSingleSession(Id talkId) {
        TaskStackBuilder taskBuilder = createBaseTaskStackBuilder();
        Intent talkDetailIntent = new Intent(context, TalkDetailsActivity.class);
        talkDetailIntent.putExtra(TalkDetailsActivity.EXTRA_TALK_ID, talkId.toString());
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
            NotificationCompat.Builder notificationBuilder, Session session) {
        String roomName = getDisplayedRooms(session);
        StringBuilder bigTextBuilder = new StringBuilder()
                .append(getDisplayedSpeakers(session))
                .append(context.getString(R.string.session_notification_starting_in, roomName));
        return new NotificationCompat.BigTextStyle(notificationBuilder)
                .setBigContentTitle(session.name())
                .bigText(bigTextBuilder.toString());
    }

    private String getDisplayedSpeakers(Session session) {
        if (!(session instanceof Talk)) {
            return "";
        }
        Talk talk = (Talk) session;
        return context.getString(R.string.session_notification_starting_by, talk.speakersNames()) + "\n";
    }

    private NotificationCompat.InboxStyle createInboxStyleRichNotification(
            NotificationCompat.Builder notificationBuilder, List<? extends Session> sessions) {
        String bigContentTitle = createSummaryTitle(sessions.size());
        NotificationCompat.InboxStyle richNotification = new NotificationCompat.InboxStyle(notificationBuilder)
                .setBigContentTitle(bigContentTitle);
        for (Session session : sessions) {
            richNotification.addLine(
                    context.getString(
                            R.string.room_session_notification,
                            getDisplayedRooms(session),
                            session.name()
                    )
            );
        }

        return richNotification;
    }

    private String createSummaryTitle(int talksCount) {
        return context.getString(R.string.session_notification_count_starting, talksCount);
    }
}
