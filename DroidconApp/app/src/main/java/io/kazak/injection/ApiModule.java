package io.kazak.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.kazak.api.DummyApi;
import io.kazak.api.KazakApi;

@Module
public class ApiModule {

    @Provides
    @Singleton
    KazakApi providesApi() {
        return new DummyApi();
    }

}
