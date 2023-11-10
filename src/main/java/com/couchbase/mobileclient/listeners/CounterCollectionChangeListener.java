package com.couchbase.mobileclient.listeners;

import com.couchbase.lite.CollectionChange;
import com.couchbase.lite.CollectionChangeListener;
import com.couchbase.mobileclient.triggers.CountAndTimeTrigger;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CounterCollectionChangeListener implements CollectionChangeListener {
    static final CountAndTimeTrigger.TriggerConfig DEFAULT_CONFIG = CountAndTimeTrigger.TriggerConfig.builder().interval(Duration.ofMinutes(1)).batchSize(10_000L).build();

    final CountAndTimeTrigger trigger;

    public CounterCollectionChangeListener() {
        this(DEFAULT_CONFIG);
    }
    public CounterCollectionChangeListener(CountAndTimeTrigger.TriggerConfig config) {
        this.trigger = new CountAndTimeTrigger(config);
    }

    @Override
    public void changed(CollectionChange change) {
        long totalChanges = change.getDocumentIDs().size();
        if(trigger.onNextElement(totalChanges)) {
            log.info("Synced {} docs after {} minutes", trigger.getCounter().get(), TimeUnit.MINUTES.toMinutes(trigger.getDuration().getTotalTimeMillis()));
        }
    }

}
