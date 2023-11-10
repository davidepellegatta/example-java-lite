package com.couchbase.mobileclient.retry;

import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.BackOffExecution;

import java.util.Timer;
import java.util.TimerTask;

public class ExponentialBackoffRetryStrategy implements RetryStrategy {


    final BackOff backOff;
    BackOffExecution currentExecution;
    final Timer retryTimer = new Timer();

    public ExponentialBackoffRetryStrategy(BackOff backOff) {
        this.backOff = backOff;
    }

    private long next() {
        return currentExecution.nextBackOff();
    }


    public void onNext(TimerTask callback) {
        retryTimer.schedule(callback, next());
    }

    @Override
    public void onNext(Runnable callback) {

    }

    @Override
    public void reset() {
        retryTimer.cancel();
        currentExecution = backOff.start(); // reset backoff policy
        //retryTimer.purge();
    }
}
