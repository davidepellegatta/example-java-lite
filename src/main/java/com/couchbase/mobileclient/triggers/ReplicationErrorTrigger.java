package com.couchbase.mobileclient.triggers;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.ReplicatorActivityLevel;
import com.couchbase.lite.ReplicatorChange;

import java.util.Objects;

import static com.couchbase.mobileclient.replicator.ReplicationErrorHandler.ErrorCode.TEMPORAL_ERROR_CODES;

public class ReplicationErrorTrigger implements Trigger<ReplicatorChange> {


    @Override
    public boolean onNextElement(ReplicatorChange element) {
        return  Objects.nonNull(element) &&
                Objects.nonNull(element.getStatus().getError()) &&
                onError(element.getStatus().getError()) &&
                element.getStatus().getActivityLevel().equals(ReplicatorActivityLevel.STOPPED);
    }

    protected boolean onError(CouchbaseLiteException exception) {
        return !TEMPORAL_ERROR_CODES.contains(exception.getCode());
    }

    @Override
    public void reset() { }
}
