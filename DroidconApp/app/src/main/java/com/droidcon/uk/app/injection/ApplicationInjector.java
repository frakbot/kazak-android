package com.droidcon.uk.app.injection;

import com.droidcon.uk.app.ScheduleActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        RepositoriesModule.class
})
public interface ApplicationInjector {
    void inject(ScheduleActivity injectable);
}
