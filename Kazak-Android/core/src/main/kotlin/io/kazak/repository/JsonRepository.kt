package io.kazak.repository

import rx.Observable

public interface JsonRepository {

    fun store(json: String): Observable<Unit>

    fun read(): Observable<String>

}
