package io.kazak.repository

import com.google.gson.Gson
import io.kazak.model.FavoriteSessions
import rx.Observable
import java.io.File
import java.io.FileWriter

public class AndroidFavoritesRepository(val files: File, val gson: Gson) : FavoriteSessionsRepository {

    private val LOCAL_FILE_NAME = "favorites.json"

    override fun store(favorites: FavoriteSessions): Observable<Unit> {
        return Observable.create {
            files.listFiles()
                    .filter { it.name == LOCAL_FILE_NAME }
                    .map { FileWriter(it) }
                    .first()
                    .buffered()
                    .use { it.write(gson.toJson(favorites)) }
            it.onCompleted()
        }
    }

    override fun read(): Observable<FavoriteSessions> {
        if (!fileExists(LOCAL_FILE_NAME)) {
            return Observable.just(FavoriteSessions(emptyMap()))
        }
        return Observable.create {
            val json = files.listFiles()
                    .filter { it.name == LOCAL_FILE_NAME }
                    .first()
                    .bufferedReader()
                    .useLines {
                        it.fold(StringBuffer(), { buffer, line -> buffer.append(line) })
                    }.toString()
            it.onNext(
                    gson.fromJson(json, javaClass<FavoriteSessions>())
            )
            it.onCompleted()
        }
    }

    private fun fileExists(fileName: String): Boolean {
        return files.listFiles().map { it.name }.contains(fileName)
    }

}
