package com.example.angel.myfit.basichistoryapi.observable;

/**
 * Created by angel on 2016-04-22.
 */
import com.example.angel.myfit.basichistoryapi.model.WorkoutReport;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by chris.black on 5/4/15.
 *
 * http://blog.danlew.net/2014/09/15/grokking-rxjava-part-1/
 */
public class WorkoutObservable {
    public static Observable<WorkoutReport> downloadFileObservable() {
        return Observable.create(
                new Observable.OnSubscribe<WorkoutReport>() {
                    @Override
                    public void call(Subscriber<? super WorkoutReport> sub) {
                        WorkoutReport report = new WorkoutReport();
                        sub.onNext(report);
                        sub.onCompleted();
                    }
                }
        );
    }
}