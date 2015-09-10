package io.kazak.injection;

import android.content.Context;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.kazak.api.KazakApi;
import io.kazak.repository.AndroidFavoritesRepository;
import io.kazak.repository.AndroidJsonRepository;
import io.kazak.repository.DataRepository;
import io.kazak.repository.FavoriteSessionsRepository;
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
    DataRepository providesDataRepository(KazakApi api, FavoriteSessionsRepository favoriteSessionsRepository) {
        return new KazakDataRepository(api, favoriteSessionsRepository);
    }

    @Provides
    @Singleton
    JsonRepository providesJsonRepository() {
        return new AndroidJsonRepository(context.getAssets(), context.getFilesDir());
    }

    @Provides
    @Singleton
    FavoriteSessionsRepository providesFavoriteSessionsRepository(Gson gson) {
        return new AndroidFavoritesRepository(context.getFilesDir(), gson);
    }

}
