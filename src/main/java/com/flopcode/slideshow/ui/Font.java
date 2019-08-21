package com.flopcode.slideshow.ui;

import java.awt.Component;
import java.awt.FontMetrics;

public class Font {
    public final java.awt.Font font;
    public final FontMetrics metrics;

    public Font(Component c, java.awt.Font font) {
        this.font = font;
        this.metrics = c.getFontMetrics(font);
    }
}
