package uk.co.droidcon.kazak.injection;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private Context appContext;

    public ApplicationModule(Context context) {
        this.appContext = context;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return appContext;
    }


}
