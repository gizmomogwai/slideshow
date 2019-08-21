package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.Moon;

import java.awt.Graphics2D;

public class MoonUI implements UI {
    final Moon moon;

    public MoonUI(Moon moon) {
        this.moon = moon;
    }

    public void render(Gfx gfx, Graphics2D g) {
        moon.getPhase().render(g);
    }
}
