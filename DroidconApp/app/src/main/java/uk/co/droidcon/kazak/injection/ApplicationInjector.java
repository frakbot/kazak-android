package uk.co.droidcon.kazak.injection;

import javax.inject.Singleton;

import dagger.Component;
import uk.co.droidcon.kazak.DebugActivity;
import uk.co.droidcon.kazak.schedule.ScheduleActivity;

@Singleton
@Component(modules = {
        ApplicationModule.class,
        RepositoriesModule.class,
        ApiModule.class,
        NotificationsModule.class
})
public interface ApplicationInjector {
    void inject(ScheduleActivity injectable);
    void inject(DebugActivity injectable);
}
