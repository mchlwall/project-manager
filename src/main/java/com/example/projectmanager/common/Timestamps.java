package com.example.projectmanager.common;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class Timestamps {

    // Fixed width so stored values sort lexicographically in chronological order
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC);

    private Timestamps() {
    }

    public static String format(Instant instant) {
        return FORMAT.format(instant);
    }

    public static Instant parse(String value) {
        return Instant.parse(value);
    }

    public static Instant now(Clock clock) {
        return clock.instant().truncatedTo(ChronoUnit.MILLIS);
    }

    /** Strictly later than {@code previous}, even if called within the same millisecond. */
    public static Instant nextAfter(Instant previous, Clock clock) {
        Instant now = now(clock);
        return now.isAfter(previous) ? now : previous.plusMillis(1);
    }
}
