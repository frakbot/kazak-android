package io.kazak.repository


import android.content.res.AssetManager
import rx.Observable
import java.io.File
import java.io.FileWriter

public class AndroidJsonRepository(val assets: AssetManager, val files: File) : JsonRepository {

    private val LOCAL_FILE_NAME = "schedule.json"

    override fun store(json: String): Observable<Unit> {
        return Observable.create {
            files.listFiles()
                    .filter { it.name == LOCAL_FILE_NAME }
                    .map { FileWriter(it) }
                    .first()
                    .buffered()
                    .use { it.write(json) }
            it.onCompleted()
        }
    }

    override fun read(): Observable<String> {
        if (fileExists(LOCAL_FILE_NAME)) {
            return readFromFile(LOCAL_FILE_NAME)
        } else {
            return readFromAssets(LOCAL_FILE_NAME)
        }
    }

    private fun fileExists(fileName: String): Boolean {
        return files.listFiles().any { it.name == fileName }
    }

    private fun readFromFile(fileName: String): Observable<String> {
        return Observable.create {
            it.onNext(
                    files.listFiles()
                            .filter { it.name == fileName }
                            .first()
                            .bufferedReader()
                            .useLines {
                                it.fold(StringBuffer(), { buffer, line -> buffer.append(line) })
                            }.toString()
            )
            it.onCompleted()
        }
    }

    private fun readFromAssets(fileName: String): Observable<String> {
        return Observable.create {
            it.onNext(
                    assets.open("json/" + fileName)
                            .bufferedReader()
                            .useLines {
                                it.fold(StringBuffer(), { buffer, line -> buffer.append(line) })
                            }.toString()
            )
            it.onCompleted()
        }
    }

}
