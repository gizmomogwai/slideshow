package com.flopcode.slideshow;

import java.time.Duration;

import static java.time.Duration.ofMinutes;

public class Constants {
    public static final Duration SLIDESHOW_NEXT_IMAGE = ofMinutes(1);
    public static final Duration WEATHER_POLL_CYCLE = ofMinutes(5);
    public static final Duration REFRESH_DISPLAY = Duration.ofSeconds(50);
}
