package com.flopcode.slideshow.clock;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface Clock {
    LocalDate date();

    LocalDateTime time();
}
