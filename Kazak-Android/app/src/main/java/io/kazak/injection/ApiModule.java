package io.kazak.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.kazak.api.DummyApi;
import io.kazak.api.KazakApi;
import io.kazak.api.json.JsonKazakApi;
import retrofit.Endpoint;
import retrofit.RestAdapter;

import static retrofit.RestAdapter.*;

@Module
public class ApiModule {

    @Provides
    @Singleton
    KazakApi providesApi() {
        return new DummyApi();
    }

    @Provides
    @Singleton
    JsonKazakApi providesJsonApi(RestAdapter adapter) {
        return adapter.create(JsonKazakApi.class);
    }

    RestAdapter provideRestAdapter() {
        return new Builder()
                .setLogLevel(LogLevel.BASIC)
                .setEndpoint(
                        new Endpoint() {
                            @Override
                            public String getUrl() {
                                return "https://kazak.herokuapp.com/api";
                            }

                            @Override
                            public String getName() {
                                return "KazakAPI";
                            }
                        })
                .build();
    }

}
