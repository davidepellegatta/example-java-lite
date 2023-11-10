package com.couchbase.mobileclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;

@Slf4j
public class BackoffRetryTest {
    public static void main(String ...args) {
        int maxRetries = 100;
        ExponentialBackOff backOff = new ExponentialBackOff();

        log.info("Backoff Policy: {}, {}, {}, {}", backOff.getMaxElapsedTime(),backOff.getMaxInterval(),backOff.getMaxInterval(), backOff.getMultiplier());
        BackOffExecution execution = backOff.start();

        for(int i = 0; i < maxRetries ; i++) {
            log.info("{} - {}", i,execution.nextBackOff());
        }
    }
}
