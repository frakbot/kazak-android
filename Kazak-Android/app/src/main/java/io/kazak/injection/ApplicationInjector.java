package io.kazak.injection;

import javax.inject.Singleton;

import dagger.Component;
import io.kazak.login.LoginActivity;
import io.kazak.schedule.ScheduleActivity;

@Singleton
@Component(modules = {
        ApiModule.class,
        AuthModule.class,
        RepositoriesModule.class
})
public interface ApplicationInjector {
    void inject(ScheduleActivity injectable);
    void inject(LoginActivity injectable);
}
