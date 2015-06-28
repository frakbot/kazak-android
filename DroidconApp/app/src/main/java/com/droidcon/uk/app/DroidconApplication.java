package com.droidcon.uk.app;

import android.app.Application;

import com.droidcon.uk.app.injection.ApplicationInjector;
import com.droidcon.uk.app.injection.DaggerApplicationInjector;

public class DroidconApplication extends Application {

    private static ApplicationInjector applicationInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationInjector = DaggerApplicationInjector.create();
    }

    public static ApplicationInjector injector() {
        if (applicationInjector == null) {
            throw new IllegalStateException("The injector is not initialised, it probably means the application is not instantiated yet");
        }
        return applicationInjector;
    }
}
