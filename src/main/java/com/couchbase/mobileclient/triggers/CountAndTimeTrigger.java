package com.couchbase.mobileclient.triggers;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Data
@Slf4j
public class CountAndTimeTrigger implements Trigger<Long> {
    static final TriggerConfig DEFAULT_CONFIG = TriggerConfig.builder().build();

    final TriggerConfig config;

    final AtomicLong iteration = new AtomicLong(0L);
    final AtomicLong counter = new AtomicLong();
    final StopWatch duration = new StopWatch("iteration-0");


    public CountAndTimeTrigger() {
        this(DEFAULT_CONFIG);
    }
    public CountAndTimeTrigger(TriggerConfig config) {
        this.config = config;
        resetWatch();
    }

    private void resetWatch() {
        if ( duration.isRunning() ) {
            duration.stop();
        }
        duration.start("iteration-"+iteration.get());
    }

    public void start() {
        duration.start("iteration-"+ iteration.get());
    }

    public boolean onNextElement(Long delta) {
        long total = counter.addAndGet(delta);
        long it = total/config.getBatchSize();
        boolean firedTrigger = (it != iteration.get() || duration.getTaskInfo().length > 0 && duration.getLastTaskTimeMillis() > config.getInterval().toMillis());

        if(firedTrigger) {
            iteration.set(it);
            resetWatch();
        }
        return firedTrigger;
    }

    public boolean onNextElement() {
        return onNextElement(1L);
    }

    @Override
    public void reset() {
        //TODO
    }

    @Data
    @Builder
    public static class TriggerConfig {
        @Builder.Default
        long batchSize = 10_000L;
        @Builder.Default
        Duration interval = Duration.ofMinutes(1);
    }

}
