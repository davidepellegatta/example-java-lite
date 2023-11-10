package com.couchbase.mobileclient.utils;

import lombok.SneakyThrows;

import java.time.Duration;

public class PauseUtils {

    @SneakyThrows
    public static void sleep(Duration duration) {
        Thread.sleep(duration.toMillis());
    }
}
