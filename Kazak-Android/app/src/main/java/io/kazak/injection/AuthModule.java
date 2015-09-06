package io.kazak.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.kazak.BuildConfig;
import io.kazak.auth.FirebaseKazakAuth;
import io.kazak.auth.KazakAuth;

@Module
public class AuthModule {

    @Provides
    @Singleton
    KazakAuth providesAuth() {
        return new FirebaseKazakAuth(BuildConfig.AUTH_URL);
    }

}
