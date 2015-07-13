package uk.co.droidcon.kazak.injection;

import javax.inject.Singleton;

import dagger.Component;
import uk.co.droidcon.kazak.ScheduleActivity;

@Singleton
@Component(modules = {
        RepositoriesModule.class
})
public interface ApplicationInjector {
    void inject(ScheduleActivity injectable);
}
