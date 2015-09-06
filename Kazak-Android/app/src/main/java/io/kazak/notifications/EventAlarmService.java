package io.kazak.notifications;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.kazak.KazakApplication;
import io.kazak.model.Session;
import io.kazak.repository.DataRepository;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class EventAlarmService extends IntentService {

    private static final int NOTIFICATION_INTERVAL_MINUTES = 10;

    @Inject
    DataRepository dataRepository;

    public EventAlarmService() {
        super(EventAlarmService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        KazakApplication.injector().inject(this);

        final NotificationCreator notificationCreator = new NotificationCreator(this);
        final Notifier notifier = Notifier.from(this);

        Calendar calendar = Calendar.getInstance();
        final Date now = calendar.getTime();
        calendar.add(Calendar.MINUTE, NOTIFICATION_INTERVAL_MINUTES);
        final Date notificationInterval = calendar.getTime();

        Observable<? extends Session> sessions = dataRepository
                .getFavorites()
                .take(1)
                .map(sortByStartingTime())
                .flatMap(
                        new Func1<List<? extends Session>, Observable<? extends Session>>() {
                            @Override
                            public Observable<? extends Session> call(List<? extends Session> sessions) {
                                return Observable.from(sessions);
                            }
                        }
                );

        sessions.filter(startingIn(now, notificationInterval))
                .toList()
                .map(createNotifications(notificationCreator))
                .subscribe(displayNotifications(notifier));

        sessions.filter(startingAfter(notificationInterval))
                .take(1)
                .subscribe(scheduleNextAlarm());
    }

    private Func1<List<? extends Session>, List<? extends Session>> sortByStartingTime() {
        return new Func1<List<? extends Session>, List<? extends Session>>() {
            @Override
            public List<? extends Session> call(List<? extends Session> sessions) {
                Collections.sort(
                        sessions, new Comparator<Session>() {
                            @Override
                            public int compare(Session firstTalk, Session secondTalk) {
                                Date firstTalkStart = firstTalk.timeSlot().getStart();
                                Date secondTalkStart = secondTalk.timeSlot().getStart();
                                return firstTalkStart.compareTo(secondTalkStart);
                            }
                        }
                );
                return sessions;
            }
        };
    }

    private Func1<Session, Boolean> startingIn(final Date now, final Date notificationInterval) {
        return new Func1<Session, Boolean>() {
            @Override
            public Boolean call(Session session) {
                Date sessionStart = session.timeSlot().getStart();
                return now.before(sessionStart) && (sessionStart.before(notificationInterval) || sessionStart.equals(notificationInterval));
            }
        };
    }

    private Func1<List<? extends Session>, List<Notification>> createNotifications(final NotificationCreator notificationCreator) {
        return new Func1<List<? extends Session>, List<Notification>>() {
            @Override
            public List<Notification> call(List<? extends Session> sessions) {
                return notificationCreator.createFrom(sessions);
            }
        };
    }

    private Action1<List<Notification>> displayNotifications(final Notifier notifier) {
        return new Action1<List<Notification>>() {
            @Override
            public void call(List<Notification> notifications) {
                notifier.showNotifications(notifications);
            }
        };
    }

    private Func1<Session, Boolean> startingAfter(final Date notificationInterval) {
        return new Func1<Session, Boolean>() {
            @Override
            public Boolean call(Session session) {
                Date sessionStart = session.timeSlot().getStart();
                return sessionStart.after(notificationInterval);
            }
        };
    }

    private Action1<Session> scheduleNextAlarm() {
        return new Action1<Session>() {
            @Override
            public void call(Session session) {
                Date talkStart = session.timeSlot().getStart();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(talkStart);
                calendar.add(Calendar.MINUTE, -NOTIFICATION_INTERVAL_MINUTES);

                Intent serviceIntent = new Intent(EventAlarmService.this, EventAlarmService.class);
                PendingIntent pendingIntent = PendingIntent.getService(EventAlarmService.this, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        };
    }

}
