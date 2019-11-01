package com.flopcode.slideshow.clock;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RealClock implements Clock {
    @Override
    public LocalDate date() {
        return LocalDate.now();
    }

    @Override
    public LocalDateTime time() {
        return LocalDateTime.now();
    }
}
