package com.couchbase.mobileclient.replicator;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorActivityLevel;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.mobileclient.triggers.CountAndTimeTrigger;
import com.couchbase.mobileclient.retry.RetryStrategy;

import java.util.Objects;
import java.util.Set;

import static com.couchbase.lite.ReplicatorActivityLevel.*;
import static com.couchbase.mobileclient.replicator.ReplicationErrorHandler.ErrorCode.*;


public class ReplicationErrorHandler {
    private final static Set<ReplicatorActivityLevel> HEALTHY_STATUS = Set.of(BUSY, IDLE, OFFLINE); //OFFLINE + errors is a healthy status?

    final RetryStrategy retry;
    final CountAndTimeTrigger trigger = new CountAndTimeTrigger();



    public ReplicationErrorHandler(RetryStrategy retry) {
        this.retry = retry;
    }

    protected void reset() {
        retry.reset();
        trigger.reset();
    }

    protected void retryOnError(Replicator replicator, CouchbaseLiteException ex) {
        if(!TEMPORAL_ERROR_CODES.contains(ex.getCode()) && replicator.getStatus().getActivityLevel().equals(STOPPED)) {
            retry.onNext(()->replicator.start());
        }
    }

    public void onError(ReplicatorChange change) {
        CouchbaseLiteException ex = change.getStatus().getError();
        boolean fired = Objects.nonNull(ex);
        if(fired) {
            //do on error ...
            retryOnError(change.getReplicator(), ex);
        } else if (HEALTHY_STATUS.contains(change.getStatus().getActivityLevel())) {
            reset();
        }
    }

    public static class ErrorCode {
        public static final int REQUEST_TIMEOUT = 408;
        public static final int TOO_MANY_REQUESTS = 429;
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int BAD_GATEWAY = 502;
        public static final int SERVICE_UNAVAILABLE = 503;
        public static final int GATEWAY_TIMEOUT = 504;
        public static final int DNS_RESOLUTION_ERROR = 1001;

        public final static Set<Integer> TEMPORAL_ERROR_CODES = Set.of(REQUEST_TIMEOUT, TOO_MANY_REQUESTS, INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT, DNS_RESOLUTION_ERROR);

    }

}
