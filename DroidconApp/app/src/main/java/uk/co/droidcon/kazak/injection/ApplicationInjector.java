package uk.co.droidcon.kazak.injection;

import javax.inject.Singleton;

import dagger.Component;
import uk.co.droidcon.kazak.schedule.ScheduleActivity;

@Singleton
@Component(modules = {
        ApiModule.class,
        RepositoriesModule.class
})
public interface ApplicationInjector {
    void inject(ScheduleActivity injectable);
}
