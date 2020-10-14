package com.flopcode.slideshow;

import java.time.Duration;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

public class Constants {
    public static Duration SLIDESHOW_NEXT_IMAGE;
    public static Duration WEATHER_POLL_CYCLE;
    public static Duration REFRESH_DISPLAY;

    static {
        release();
    }

    private static void release() {
        SLIDESHOW_NEXT_IMAGE = ofMinutes(1);
        WEATHER_POLL_CYCLE = ofMinutes(5);
        REFRESH_DISPLAY = ofSeconds(50);
    }

    private static void debug() {
        SLIDESHOW_NEXT_IMAGE = ofSeconds(15);
        WEATHER_POLL_CYCLE = ofSeconds(15);
        REFRESH_DISPLAY = ofSeconds(50);
    }
}
