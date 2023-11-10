package com.couchbase.mobileclient.triggers;

import com.couchbase.lite.ReplicatorActivityLevel;

import java.util.Set;

import static com.couchbase.lite.ReplicatorActivityLevel.*;


public class FinalStatusTrigger implements Trigger<ReplicatorActivityLevel>{
    public static final Set<ReplicatorActivityLevel> FINAL_STATUS = Set.of(STOPPED, IDLE);

    @Override
    public boolean onNextElement(ReplicatorActivityLevel currentStatus) {
        return FINAL_STATUS.contains(currentStatus);
    }

    @Override
    public boolean onNextElement() {
        return false;
    }

    @Override
    public void reset() { }
}
