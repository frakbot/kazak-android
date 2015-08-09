package io.kazak;

import android.app.Application;

import io.kazak.injection.ApplicationInjector;
import io.kazak.injection.DaggerApplicationInjector;

public class KazakApplication extends Application {

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
