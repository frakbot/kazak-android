package io.kazak.session;

import rx.Observer;

public class SimpleObserver<T> implements Observer<T> {
    @Override
    public void onCompleted() {
        //Space to rent
    }

    @Override
    public void onError(Throwable e) {
        throw new IllegalStateException(e);
    }

    @Override
    public void onNext(T o) {
        //Space to rent
    }
}
