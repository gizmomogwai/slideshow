package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.clock.Clock;

import java.awt.Color;
import java.awt.Graphics2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import static java.lang.String.format;
import static java.time.format.TextStyle.SHORT_STANDALONE;

public class CalendarDateTimeUI implements UI {
    private final Locale locale;
    private final Clock clock;
    private final Font font;

    public CalendarDateTimeUI(Clock clock, Font font) {
        this.clock = clock;
        this.font = font;
        locale = Locale.ENGLISH;
    }

    public void render(Gfx gfx, Graphics2D g) {
        gfx.setFont(font.font);
        gfx.setColor(Color.WHITE);
        LocalDate date = clock.date();
        String dateFirstLine = date.getDayOfWeek().getDisplayName(SHORT_STANDALONE, locale) + " " + date.getDayOfMonth() + ".";
        String dateSecondLine = date.getMonth().getDisplayName(SHORT_STANDALONE, locale) + " " + date.getYear();
        gfx.centerString(dateFirstLine, 130, 50);
        gfx.centerString(dateSecondLine, 130, 100);

        LocalDateTime now = clock.time();
        gfx.centerString(format("%02d", now.getHour()), 290, 50);
        gfx.centerString(format("%02d", now.getMinute()), 290, 100);
    }
}
