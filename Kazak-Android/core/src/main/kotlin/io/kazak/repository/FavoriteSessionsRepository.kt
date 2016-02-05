package io.kazak.repository

import io.kazak.model.FavoriteSessions
import rx.Observable

interface FavoriteSessionsRepository {

    fun store(favorites: FavoriteSessions): Observable<Unit>

    fun read(): Observable<FavoriteSessions>

}
