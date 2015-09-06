package io.kazak.injection;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.kazak.api.KazakApi;
import io.kazak.repository.AndroidJsonRepository;
import io.kazak.repository.DataRepository;
import io.kazak.repository.JsonRepository;
import io.kazak.repository.KazakDataRepository;

@Module
public class RepositoriesModule {

    private final Context context;

    public RepositoriesModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    DataRepository providesDataRepository(KazakApi api) {
        return new KazakDataRepository(api);
    }

    @Provides
    @Singleton
    JsonRepository providesJsonRepository() {
        return new AndroidJsonRepository(context.getAssets(), context.getFilesDir());
    }

}
