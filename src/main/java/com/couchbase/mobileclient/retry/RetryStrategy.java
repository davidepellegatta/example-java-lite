package com.couchbase.mobileclient.retry;

import com.couchbase.mobileclient.utils.Resetable;

public interface RetryStrategy extends Resetable {

    void onNext(Runnable callback);

    void reset();

}
