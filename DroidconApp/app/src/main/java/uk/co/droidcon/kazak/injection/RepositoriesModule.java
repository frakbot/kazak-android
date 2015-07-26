package uk.co.droidcon.kazak.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.droidcon.kazak.repository.DataRepository;
import uk.co.droidcon.kazak.repository.DroidconDataRepository;

@Module
public class RepositoriesModule {

    @Provides
    @Singleton
    DataRepository providesDataRepository() {
        return new DroidconDataRepository();
    }

}
