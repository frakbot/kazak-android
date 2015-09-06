package io.kazak.injection;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.kazak.BuildConfig;
import io.kazak.api.KazakApi;
import io.kazak.api.PersistedKazakApi;
import io.kazak.api.json.JsonKazakApi;
import io.kazak.repository.JsonRepository;
import retrofit.Endpoint;
import retrofit.RestAdapter;

import static retrofit.RestAdapter.Builder;
import static retrofit.RestAdapter.LogLevel;

@Module
public class ApiModule {

    @Provides
    @Singleton
    Gson providesGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    KazakApi providesApi(JsonKazakApi remoteApi, Gson gson, JsonRepository jsonRepository) {
        return new PersistedKazakApi(remoteApi, jsonRepository, gson);
    }

    @Provides
    @Singleton
    JsonKazakApi providesJsonApi(RestAdapter adapter) {
        return adapter.create(JsonKazakApi.class);
    }

    @Provides
    @Singleton
    RestAdapter provideRestAdapter() {
        return new Builder()
                .setLogLevel(LogLevel.BASIC)
                .setEndpoint(
                        new Endpoint() {
                            @Override
                            public String getUrl() {
                                return BuildConfig.ENDPOINT_URL;
                            }

                            @Override
                            public String getName() {
                                return "KazakAPI";
                            }
                        })
                .build();
    }

}
