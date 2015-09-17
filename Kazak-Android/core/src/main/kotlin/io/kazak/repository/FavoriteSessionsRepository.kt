package io.kazak.repository

import io.kazak.model.FavoriteSessions
import rx.Observable

public interface FavoriteSessionsRepository {

    fun store(favorites: FavoriteSessions): Observable<Unit>

    fun read(): Observable<FavoriteSessions>

}
