package uk.co.droidcon.kazak.rx

import rx.Observable
import rx.Subscriber

public class InfiniteOperator<T> : Observable.Operator<T, T> {

    override fun call(subscriber: Subscriber<in T>): Subscriber<in T> {
        return object : Subscriber<T>() {
            override fun onCompleted() {
                //Swallow
            }

            override fun onError(e: Throwable) {
                subscriber.onError(e)
            }

            override fun onNext(t: T) {
                subscriber.onNext(t)
            }
        }
    }

}
