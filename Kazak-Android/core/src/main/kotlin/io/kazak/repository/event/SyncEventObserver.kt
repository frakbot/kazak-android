package io.kazak.repository.event

import rx.Observer
import rx.subjects.BehaviorSubject

public class SyncEventObserver<T>(val subject: BehaviorSubject<T>, val syncSubject: BehaviorSubject<SyncEvent>) : Observer<T> {
    override fun onError(e: Throwable?) {
        syncSubject.onNext(SyncEvent(SyncState.ERROR, e))
    }

    override fun onNext(t: T) {
        subject.onNext(t)
        syncSubject.onNext(SyncEvent(SyncState.IDLE))
    }

    override fun onCompleted() {
        syncSubject.onNext(SyncEvent(SyncState.IDLE))
    }
}
