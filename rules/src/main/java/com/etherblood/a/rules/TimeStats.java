package com.etherblood.a.rules;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TimeStats {

    private static final TimeStats INSTANCE = new TimeStats();

    private final Map<String, Long> map = new ConcurrentHashMap<>();

    public Stopwatch time(String key) {
        long start = System.nanoTime();
        return () -> {
                map.compute(key, (k, prev) -> {
                    long duration = System.nanoTime() - start;
                    return prev == null ? duration : prev + duration;
                });
        };
    }

    public List<String> toStatsStrings() {
        Comparator<Map.Entry<String, Long>> comparator = Comparator.comparingLong(Map.Entry::getValue);
        return map.entrySet().stream()
                .sorted(comparator.reversed())
                .map(entry -> entry.getKey() + ": " + humanReadableNanos(entry.getValue()))
                .collect(Collectors.toList());
    }

    public static String humanReadableNanos(long nanos) {
        int count = 0;
        while (nanos > 10000 && count < 3) {
            nanos /= 1000;
            count++;
        }
        if (count == 3) {
            return nanos + "s";
        }
        return nanos + Character.toString("n\u00B5m".charAt(count)) + "s";
    }

    public static TimeStats get() {
        return INSTANCE;
    }
}
