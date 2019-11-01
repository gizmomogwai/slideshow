package com.flopcode.slideshow.clock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

public class FakeClock implements Clock {
    @Override
    public LocalDate date() {
        LocalDate date = LocalDate.now();
        LocalDateTime time = LocalDateTime.now();
        return LocalDate.of(date.getYear(), Month.of(time.getMinute() % 12 + 1), date.getDayOfMonth());
    }

    @Override
    public LocalDateTime time() {
        return LocalDateTime.now();
    }
}
