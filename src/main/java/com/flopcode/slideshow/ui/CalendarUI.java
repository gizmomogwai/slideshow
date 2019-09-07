package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.SlideshowCanvas;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.LocalDate;

public class CalendarUI implements UI {

    final Dimension screenSize;
    final SlideshowCanvas.Fonts fonts;
    private final SlideshowCanvas.PublicHolidays publicHolidays;
    private final CalendarDateTimeUI calendarDateTime;
    private final CalendarBackgroundUI calendarBackground;

    public CalendarUI(Dimension screenSize, SlideshowCanvas.Fonts fonts, SlideshowCanvas.PublicHolidays publicHolidays) {
        this.screenSize = screenSize;
        this.fonts = fonts;
        this.publicHolidays = publicHolidays;

        calendarBackground = new CalendarBackgroundUI();
        calendarDateTime = new CalendarDateTimeUI(fonts.dateTime);
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) {
        gfx.render(calendarBackground, 0, 0);
        gfx.render(calendarDateTime, 0, 0);
        LocalDate now = LocalDate.now();
        SlideshowCanvas.CalendarLine.render(g, 330, fonts.calendar, now, publicHolidays);
    }
}
