package io.kazak.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;

import javax.inject.Inject;
import java.util.Calendar;
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

        final Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, NOTIFICATION_INTERVAL_MINUTES);
        final Date notificationInterval = calendar.getTime();

        dataRepository
                .getSchedule()
                .take(1)
                .flatMap(getDays())
                .flatMap(getTalks())
                .filter(startingIn(now, notificationInterval))
                .toList()
                .map(createNotifiactions(notificationCreator))
                .subscribe(displayNotifications(notifier));

    }

    private Func1<Schedule, Observable<Day>> getDays() {
        return new Func1<Schedule, Observable<Day>>() {
            @Override
            public Observable<Day> call(Schedule schedule) {
                return Observable.from(schedule.getDays());
            }
        };
    }

    private Func1<Day, Observable<Talk>> getTalks() {
        return new Func1<Day, Observable<Talk>>() {
            @Override
            public Observable<Talk> call(Day day) {
                return Observable.from(day.getTalks());
            }
        };
    }

    private Func1<Talk, Boolean> startingIn(final Date now, final Date notificationInterval) {
        return new Func1<Talk, Boolean>() {
            @Override
            public Boolean call(Talk talk) {
                Date talkStart = talk.getTimeSlot().getStart();
                return now.before(talkStart) && talkStart.before(notificationInterval);
            }
        };
    }

    private Func1<List<Talk>, List<Notification>> createNotifiactions(final NotificationCreator notificationCreator) {
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
