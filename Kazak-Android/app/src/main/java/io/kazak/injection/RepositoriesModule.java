package io.kazak.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.kazak.api.KazakApi;
import io.kazak.repository.DataRepository;
import io.kazak.repository.KazakDataRepository;

@Module
public class RepositoriesModule {

    @Provides
    @Singleton
    DataRepository providesDataRepository(KazakApi api) {
        return new KazakDataRepository(api);
    }

}
