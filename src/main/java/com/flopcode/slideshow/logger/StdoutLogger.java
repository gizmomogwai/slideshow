package com.flopcode.slideshow.logger;

import static com.flopcode.slideshow.logger.Logger.Level.DEBUG;
import static com.flopcode.slideshow.logger.Logger.Level.ERROR;
import static com.flopcode.slideshow.logger.Logger.Level.INFO;
import static com.flopcode.slideshow.logger.Logger.Level.WARNING;

public class StdoutLogger implements Logger {

    final Level level;

    public StdoutLogger(Level level) {
        this.level = level;
    }

    @Override
    public Logger d(String message) {
        if (level.enabled(DEBUG)) {
            System.out.println("D " + message);
        }
        return this;
    }

    @Override
    public Logger i(String message) {
        if (level.enabled(INFO)) {
            System.out.println("I " + message);
        }
        return this;
    }

    @Override
    public Logger w(String message) {
        if (level.enabled(WARNING)) {
            System.out.println("W " + message);
        }
        return this;
    }

    @Override
    public Logger e(String message) {
        if (level.enabled(ERROR)) {
            System.out.println("E " + message);
        }
        return this;
    }
}
