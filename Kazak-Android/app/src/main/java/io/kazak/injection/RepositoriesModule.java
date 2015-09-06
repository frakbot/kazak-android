package io.kazak.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.kazak.api.KazakApi;
import io.kazak.auth.KazakAuth;
import io.kazak.repository.AuthRepository;
import io.kazak.repository.DataRepository;
import io.kazak.repository.KazakAuthRepository;
import io.kazak.repository.KazakDataRepository;

@Module
public class RepositoriesModule {

    @Provides
    @Singleton
    DataRepository providesDataRepository(KazakApi api) {
        return new KazakDataRepository(api);
    }

    @Provides
    @Singleton
    AuthRepository providesAuthRepository(KazakAuth authenticator) {
        return new KazakAuthRepository(authenticator);
    }

}
