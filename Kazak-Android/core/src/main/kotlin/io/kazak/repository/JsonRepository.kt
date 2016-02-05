package io.kazak.repository

import rx.Observable

interface JsonRepository {

    fun store(json: String): Observable<Unit>

    fun read(): Observable<String>

}
