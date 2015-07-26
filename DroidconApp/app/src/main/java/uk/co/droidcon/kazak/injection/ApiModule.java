package uk.co.droidcon.kazak.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.droidcon.kazak.api.DroidconApi;
import uk.co.droidcon.kazak.api.DummyApi;

@Module
public class ApiModule {

    @Provides
    @Singleton
    DroidconApi providesApi() {
        return new DummyApi();
    }

}
