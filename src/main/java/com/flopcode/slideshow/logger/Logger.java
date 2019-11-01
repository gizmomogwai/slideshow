package com.flopcode.slideshow.logger;

public interface Logger {
    enum Level {
        DEBUG(3),
        INFO(2),
        WARNING(1),
        ERROR(0);
        public final int value;

        Level(int value) {
            this.value = value;
        }

        public boolean enabled(Level l) {
            return value >= l.value;
        }
    }

    Logger d(String message);

    Logger i(String message);

    Logger w(String message);

    Logger e(String message);
}
