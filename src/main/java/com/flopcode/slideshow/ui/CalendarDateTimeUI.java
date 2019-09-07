package com.flopcode.slideshow.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import static java.lang.String.format;
import static java.time.format.TextStyle.SHORT_STANDALONE;

public class CalendarDateTimeUI implements UI {
    private final Locale locale;
    private final Font font;

    public CalendarDateTimeUI(Font font) {
        this.font = font;
        locale = Locale.ENGLISH;
    }

    public void render(Gfx gfx, Graphics2D g) {
        gfx.setFont(font.font);
        gfx.setColor(Color.WHITE);
        LocalDate now = LocalDate.now();
        String dateFirstLine = now.getDayOfWeek().getDisplayName(SHORT_STANDALONE, locale) + " " + now.getDayOfMonth() + ".";
        String dateSecondLine = now.getMonth().getDisplayName(SHORT_STANDALONE, locale) + " " + now.getYear();
        gfx.centerString(dateFirstLine, 130, 50);
        gfx.centerString(dateSecondLine, 130, 100);

        LocalDateTime time = LocalDateTime.now();
        gfx.centerString(format("%02d", time.getHour()), 290, 50);
        gfx.centerString(format("%02d", time.getMinute()), 290, 100);
    }
}
