package uk.co.droidcon.kazak.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.droidcon.kazak.ADataRepository;
import uk.co.droidcon.kazak.DataRepository;

@Module
public class RepositoriesModule {

    @Provides
    @Singleton
    DataRepository providesDataRepository() {
        return new ADataRepository();
    }

}
