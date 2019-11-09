package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.SlideshowCanvas;
import com.flopcode.slideshow.WhiteboardForHandler;
import com.flopcode.slideshow.processes.Database;

import java.awt.Dimension;
import java.awt.Graphics2D;

public class StatisticsUI implements UI {
    private final Dimension screenSize;
    private final SlideshowCanvas.Fonts fonts;
    private final WhiteboardForHandler whiteboard;
    Database.Statistics statistics;

    public StatisticsUI(WhiteboardForHandler whiteboard, Dimension screenSize, SlideshowCanvas.Fonts fonts) {
        this.screenSize = screenSize;
        this.fonts = fonts;
        whiteboard.add("databaseStatistics", (key, value) -> statistics = (Database.Statistics) value);
        this.whiteboard = whiteboard;
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) throws Exception {
        if (statistics != null) {
            gfx.setFont(fonts.calendar.font);
            gfx.drawStringRightAligned("" + statistics.currentImage + " / " + statistics.filteredImages + " / " + statistics.totalImages, 0, 0);
        }
    }
}
