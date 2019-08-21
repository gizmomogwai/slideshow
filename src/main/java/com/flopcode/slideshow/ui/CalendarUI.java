package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.SlideshowCanvas;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.LocalDate;
import java.util.Locale;

public class CalendarUI implements UI {

    final Dimension screenSize;
    final SlideshowCanvas.Fonts fonts;
    private final SlideshowCanvas.PublicHolidays publicHolidays;

    public CalendarUI(Dimension screenSize, SlideshowCanvas.Fonts fonts, SlideshowCanvas.PublicHolidays publicHolidays) {
        this.screenSize = screenSize;
        this.fonts = fonts;
        this.publicHolidays = publicHolidays;
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) {
        Locale locale = Locale.ENGLISH;
        LocalDate now = LocalDate.now();

        SlideshowCanvas.CalendarBackground.render(g, screenSize);
        SlideshowCanvas.CalendarDate.render(g, fonts.subtitles, now, locale);
        SlideshowCanvas.CalendarLine.render(g, 230, fonts.calendar, now, publicHolidays);
    }
}
