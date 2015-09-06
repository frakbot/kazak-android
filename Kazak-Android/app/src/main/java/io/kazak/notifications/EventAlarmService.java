package io.kazak.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.util.Log;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.kazak.KazakApplication;
import io.kazak.model.Day;
import io.kazak.model.Schedule;
import io.kazak.model.Talk;
import io.kazak.repository.DataRepository;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class EventAlarmService extends IntentService {

    private static final int NOTIFICATION_INTERVAL_MINUTES = 10;

    @Inject
    @SuppressWarnings("checkstyle:visibilitymodifier")
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

        Observable<Talk> talks = loadTalks();

        talks.filter(startingIn(now, notificationInterval))
                .toList()
                .map(createNotifications(notificationCreator))
                .subscribe(displayNotifications(notifier));

    }

    private Observable<Talk> loadTalks() {
        return dataRepository
                .getSchedule()
                .take(1)
                .flatMap(getSortedDays())
                .flatMap(getSortedTalks());
    }

    private Func1<Schedule, Observable<Day>> getSortedDays() {
        return new Func1<Schedule, Observable<Day>>() {
            @Override
            public Observable<Day> call(Schedule schedule) {
                List<Day> days = schedule.getDays();
                Collections.sort(
                        days, new Comparator<Day>() {
                            @Override
                            public int compare(Day firstDay, Day secondDay) {
                                return firstDay.getDay().compareTo(secondDay.getDay());
                            }
                        }
                );
                return Observable.from(days);
            }
        };
    }

    private Func1<Day, Observable<Talk>> getSortedTalks() {
        return new Func1<Day, Observable<Talk>>() {
            @Override
            public Observable<Talk> call(Day day) {
                List<Talk> talks = day.getTalks();
                Collections.sort(
                        talks, new Comparator<Talk>() {
                            @Override
                            public int compare(Talk firstTalk, Talk secondTalk) {
                                Date firstTalkStart = firstTalk.getTimeSlot().getStart();
                                Date secondTalkStart = secondTalk.getTimeSlot().getStart();
                                return firstTalkStart.compareTo(secondTalkStart);
                            }
                        }
                );
                return Observable.from(talks);
            }
        };
    }

    private Func1<Talk, Boolean> startingIn(final Date now, final Date notificationInterval) {
        return new Func1<Talk, Boolean>() {
            @Override
            public Boolean call(Talk talk) {
                Date talkStart = talk.getTimeSlot().getStart();
                return now.before(talkStart) && (talkStart.before(notificationInterval) || talkStart.equals(notificationInterval));
            }
        };
    }

    private Func1<List<Talk>, List<Notification>> createNotifications(final NotificationCreator notificationCreator) {
        return new Func1<List<Talk>, List<Notification>>() {
            @Override
            public List<Notification> call(List<Talk> talks) {
                return notificationCreator.createFrom(talks);
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

}
