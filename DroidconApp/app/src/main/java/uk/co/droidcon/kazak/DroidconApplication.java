package uk.co.droidcon.kazak;

import android.app.Application;

import uk.co.droidcon.kazak.injection.ApplicationInjector;
import uk.co.droidcon.kazak.injection.DaggerApplicationInjector;

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
