package com.couchbase.mobileclient.listeners;

import com.couchbase.lite.*;
import com.couchbase.mobileclient.database.DBManager;
import com.couchbase.mobileclient.replicator.ReplicationErrorHandler;
import com.couchbase.mobileclient.triggers.CountAndTimeTrigger.TriggerConfig;
import com.couchbase.mobileclient.triggers.FinalStatusTrigger;
import com.couchbase.mobileclient.triggers.ReplicationErrorTrigger;
import com.couchbase.mobileclient.triggers.StatusCountAndTimeTrigger;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Objects;

/**
 * Intercepts
 */
@Data
@Slf4j
public class StatusChangeListener implements ReplicatorChangeListener {
    public static final TriggerConfig DEFAULT_CONFIG = TriggerConfig.builder().batchSize(500).interval(Duration.ofMinutes(1)).build();

    Runnable onExitCallback = this::defaultOnExitCallback;

    final StatusCountAndTimeTrigger trigger;
    final FinalStatusTrigger finalStatusTrigger = new FinalStatusTrigger();

    public StatusChangeListener() {
        this(new StatusCountAndTimeTrigger(DEFAULT_CONFIG));
    }

    public StatusChangeListener(StatusCountAndTimeTrigger trigger) {
        this.trigger = trigger;
    }

    public void setOnExitCallback(Runnable callback) {
        this.onExitCallback = callback;
    }

    protected void defaultOnExitCallback() {

    }

    @SneakyThrows
    @Override
    public void changed(ReplicatorChange change) {
        ReplicatorActivityLevel currentStatus = change.getStatus().getActivityLevel();

        if (trigger.onNextElement(change)) {
            log.info( "Current iter: {}, Status: {}, Sequence: {}",
                    change.getStatus().getProgress().getCompleted(),
                    currentStatus,
                    change.getStatus().getProgress());
        }

        //handling error here
        CouchbaseLiteException err = change.getStatus().getError();
        if (Objects.nonNull(err)) {
            log.error("{} - replication {}-{}: {}. ",change.getStatus().getActivityLevel(), err.getCode(), err.getDomain(),err.getClass().getSimpleName(), err);
        // TODO errorHandler.onError(change);
        }


        if (finalStatusTrigger.onNextElement(currentStatus)) {
            //handling final status: Stopped on continuous replication and no network errors/retries
            log.info("Raised replicator {} condition for ending...", currentStatus);
            onExitCallback.run();
        }

    }



}
