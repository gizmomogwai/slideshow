package com.flopcode.slideshow;

import java.awt.Component;
import java.awt.FontMetrics;

public class Font {
    public final java.awt.Font font;
    final FontMetrics metrics;

    Font(Component c, java.awt.Font font) {
        this.font = font;
        this.metrics = c.getFontMetrics(font);
    }
}
