package io.kazak.injection;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.kazak.BuildConfig;
import io.kazak.auth.FirebaseKazakAuth;
import io.kazak.auth.KazakAuth;

@Module
public class AuthModule {

    private final Context context;

    public AuthModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    KazakAuth providesAuth() {
        return new FirebaseKazakAuth(BuildConfig.AUTH_URL);
    }

}
