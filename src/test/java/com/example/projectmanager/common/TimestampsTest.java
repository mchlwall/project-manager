package com.example.projectmanager.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class TimestampsTest {

    private static final Instant NOW = Instant.parse("2026-11-10T12:00:00.000Z");

    @Test
    void nextAfterAddsOneMillisecondWhenClockHasNotMoved() {
        Clock frozen = Clock.fixed(NOW, ZoneOffset.UTC);
        Clock later = Clock.fixed(NOW.plusSeconds(5), ZoneOffset.UTC);

        assertThat(Timestamps.nextAfter(NOW, frozen)).isEqualTo(NOW.plusMillis(1));
        assertThat(Timestamps.nextAfter(NOW, later)).isEqualTo(NOW.plusSeconds(5));
    }

    @Test
    void formatAlwaysIncludesMillisecondsInUtc() {
        assertThat(Timestamps.format(NOW)).isEqualTo("2026-11-10T12:00:00.000Z");
        assertThat(Timestamps.format(NOW.plusMillis(7))).isEqualTo("2026-11-10T12:00:00.007Z");
    }
}
