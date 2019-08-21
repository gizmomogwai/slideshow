package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.SlideshowCanvas;

import java.awt.Graphics2D;

public class OnTopUI implements UI {

    private final SlideshowCanvas.Fonts fonts;
    private final CalendarUI calendarUi;
    private final MoonUI moonUi;
    private final WeatherUI weatherUi;
    private final StatisticsUI statisticsUi;

    public OnTopUI(SlideshowCanvas.Fonts fonts, CalendarUI calendarUi, MoonUI moonUi, WeatherUI weatherUi, StatisticsUI statisticsUi) {
        this.fonts = fonts;
        this.calendarUi = calendarUi;
        this.moonUi = moonUi;
        this.weatherUi = weatherUi;
        this.statisticsUi = statisticsUi;
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) throws Exception {
        gfx.render(calendarUi, 0, 0);

        gfx.render(moonUi, gfx.fromRight(80), gfx.fromTop(30));
        gfx.render(weatherUi, 0, 100);

        gfx.render(statisticsUi, gfx.fromRight(10), gfx.fromBottom(10));
    }
}
