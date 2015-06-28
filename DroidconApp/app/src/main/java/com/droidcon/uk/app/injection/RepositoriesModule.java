package com.droidcon.uk.app.injection;

import com.droidcon.uk.app.ADataRepository;
import com.droidcon.uk.app.DataRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RepositoriesModule {

    @Provides
    @Singleton
    DataRepository providesDataRepository() {
        return new ADataRepository();
    }

}
