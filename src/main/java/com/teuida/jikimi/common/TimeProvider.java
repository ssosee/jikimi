package com.teuida.jikimi.common;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimeProvider {

    private final Clock clock;

    public ZonedDateTime nowZonedDateTime() {
        return ZonedDateTime.now(clock);
    }

    public LocalDateTime nowDateTime() {
        return LocalDateTime.now(clock);
    }

    public LocalDate nowDate() {
        return LocalDate.now(clock);
    }
}
