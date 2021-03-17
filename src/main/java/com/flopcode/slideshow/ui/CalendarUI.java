package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.SlideshowCanvas;
import com.flopcode.slideshow.clock.Clock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static java.lang.String.format;

public class CalendarUI implements UI {

    private final Clock clock;

    final Dimension screenSize;
    final SlideshowCanvas.Fonts fonts;
    private final SlideshowCanvas.PublicHolidays publicHolidays;
    private final CalendarDateTimeUI calendarDateTime;
    private final CalendarBackgroundUI calendarBackground;
    private final AgeUI age;

    private static final class AgeUI implements UI {
        private final Clock clock;
        private final Font font;
        public AgeUI(Clock clock, Font font) {
            this.clock = clock;
            this.font = font;
        }
        @Override
        public void render(Gfx gfx, Graphics2D g) throws Exception {
            LocalDate birthday = LocalDate.of(2020, 11, 30);
            long weeks = ChronoUnit.WEEKS.between(birthday, clock.date());

            gfx.setFont(font.font);
            gfx.setColor(Color.WHITE);
            gfx.drawString("*", 20, 15);
            gfx.drawString(format("%d", weeks), 50, 0);
        }
    }

    public CalendarUI(Clock clock, Dimension screenSize, SlideshowCanvas.Fonts fonts, SlideshowCanvas.PublicHolidays publicHolidays) {
        this.clock = clock;
        this.screenSize = screenSize;
        this.fonts = fonts;
        this.publicHolidays = publicHolidays;

        calendarBackground = new CalendarBackgroundUI();
        calendarDateTime = new CalendarDateTimeUI(clock, fonts.dateTime);
        age = new AgeUI(clock, fonts.dateTime);
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) {
        gfx.render(calendarBackground, 0, 0);
        gfx.render(calendarDateTime, 0, 0);
        gfx.render(age, 0, 150);
        LocalDate now = clock.date();
        SlideshowCanvas.CalendarLine.render(g, 330, fonts.calendar, now, publicHolidays);
    }
}
