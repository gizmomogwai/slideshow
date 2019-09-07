package com.flopcode.slideshow.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import static java.awt.AlphaComposite.SRC_OVER;

public class CalendarBackgroundUI implements UI {
    private static final Color backgroundColor = new Color(0, 0, 0, 0.7f);

    @Override
    public void render(Gfx gfx, Graphics2D g) {
        g.setComposite(AlphaComposite.getInstance(SRC_OVER, 1.0f));
        g.setColor(backgroundColor);
        g.setStroke(new BasicStroke(1.5f));
        g.fillRect(0, 0, g.getClipBounds().width, 120);
    }
}
