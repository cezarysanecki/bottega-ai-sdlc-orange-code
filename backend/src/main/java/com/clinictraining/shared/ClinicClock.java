package com.clinictraining.shared;

import java.time.Clock;
import java.time.ZonedDateTime;

/**
 * Shared utility for retrieving clinic time.
 */
import org.springframework.stereotype.Component;

@Component
public class ClinicClock {

    private final Clock clock;

    public ClinicClock() {
        this.clock = Clock.systemDefaultZone();
    }

    public ClinicClock(Clock clock) {
        this.clock = clock;
    }

    public ZonedDateTime now() {
        return ZonedDateTime.now(clock);
    }
}
