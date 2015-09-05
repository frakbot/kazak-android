package io.kazak.injection;

import javax.inject.Singleton;

import dagger.Component;
import io.kazak.notifications.EventAlarmService;
import io.kazak.schedule.ScheduleActivity;

@Singleton
@Component(modules = {
        ApiModule.class,
        RepositoriesModule.class
})
public interface ApplicationInjector {

    void inject(ScheduleActivity injectable);

    void inject(EventAlarmService injectable);

}
