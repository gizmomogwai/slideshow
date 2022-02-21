package com.flopcode.slideshow.data.images;

import java.nio.file.Path;

public class CannotGetDateException extends RuntimeException {
    public CannotGetDateException(Path path, Exception e) {
        super("Cannot get date for path :'" + path + "'", e);
    }
}
