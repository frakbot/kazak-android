package io.kazak;

import android.app.Application;

import com.facebook.stetho.Stetho;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.kazak.injection.ApiModule;
import io.kazak.injection.ApplicationInjector;
import io.kazak.injection.AuthModule;
import io.kazak.injection.DaggerApplicationInjector;
import io.kazak.injection.RepositoriesModule;

public class KazakApplication extends Application {

    private static ApplicationInjector applicationInjector;

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "It's the right way of initialising Dagger.")
    @Override
    public void onCreate() {
        super.onCreate();
        setupStetho();
        applicationInjector = DaggerApplicationInjector.builder()
                .apiModule(new ApiModule())
                .repositoriesModule(new RepositoriesModule(this))
                .authModule(new AuthModule(this))
                .build();
    }

    private void setupStetho() {
        if (!BuildConfig.ENABLE_STETHO) {
            return;
        }
        // TODO configure OkHttp interception when we begin using it
        // See http://facebook.github.io/stetho/#integrations
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }

    public static ApplicationInjector injector() {
        if (applicationInjector == null) {
            throw new IllegalStateException("The injector is not initialised, it probably means the application is not instantiated yet");
        }
        return applicationInjector;
    }

}
