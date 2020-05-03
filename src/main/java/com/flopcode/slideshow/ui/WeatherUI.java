package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.SlideshowCanvas;
import com.flopcode.slideshow.WhiteboardForHandler;
import com.flopcode.slideshow.clock.Clock;
import com.flopcode.slideshow.data.weather.WeatherIcons;
import com.flopcode.slideshow.logger.Logger;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.time.format.DateTimeFormatter;

import static java.awt.Color.WHITE;

public class WeatherUI implements UI {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ForecastUI tomorrow;
    private final ForecastUI dayAfterTomorrow;
    private final SunriseSunsetUI sunriseSunset;
    private final CurrentConditionUI currentCondition;
    private final SlideshowCanvas.Fonts fonts;
    // private final ForecastGraphUI forecastGraph;

    public WeatherUI(Logger logger, Clock clock, WhiteboardForHandler whiteboard, SlideshowCanvas.Fonts fonts) throws Exception {
        this.fonts = fonts;
        WeatherIcons icons = new WeatherIcons(logger);

        sunriseSunset = new SunriseSunsetUI(whiteboard, icons);
        currentCondition = new CurrentConditionUI(whiteboard, icons);

        tomorrow = new ForecastUI(whiteboard, icons, (v) -> v != null ? v.dailies.get(1) : null);
        dayAfterTomorrow = new ForecastUI(whiteboard, icons, (v) -> v != null ? v.dailies.get(2) : null);
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) {
        gfx.setFont(fonts.weather.font);
        gfx.setColor(WHITE);

        gfx.render(sunriseSunset, 0, 0);

        gfx.render(currentCondition, 0, 110);

        gfx.tmp((gfx2) -> {
            gfx2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            gfx2.render(tomorrow, 0, gfx.fromTop(310));
            gfx2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            gfx2.render(dayAfterTomorrow, 0, gfx.fromTop(510));
        });
        //gfx.render(forecastGraph, 0, gfx.fromTop(600));
    }
/*
    private static class WeatherExtract {
        private final Clock clock;
        Forecast today;
        Forecast tomorrow;
        Forecast dayAfterTomorrow;
        List<Weather.Forecast> all;

        WeatherExtract(Clock clock, WhiteboardForHandler whiteboard) {
            this.clock = clock;
            whiteboard.add("weatherInfo", (key, value) -> update((Weather.WeatherInfo) value));
        }

        public void update(Weather.WeatherInfo weatherInfo) {
            if (weatherInfo == null) {
                return;
            }
            LocalDate now = clock.date();
            today = new Forecast(weatherInfo.dailies.get(0));
            tomorrow = new Forecast(weatherInfo.dailies.get(1));
            dayAfterTomorrow = new Forecast(weatherInfo.dailies.get(2));
        }

        private List<Weather.Forecast> getForecasts(Weather.WeatherInfo weatherInfo, LocalDate now) {
            // FIXME NOW HERE
            return Collections.emptyList();
        }

    }
/*
    static class HourComparator implements Comparator<Weather.Forecast> {
        private final int hour;

        HourComparator(int hour) {
            this.hour = hour;
        }

        @Override
        public int compare(Weather.Forecast o1, Weather.Forecast o2) {
            return Math.abs(o1.time.getHour() - hour) - Math.abs(o2.time.getHour() - hour);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof HourComparator)) {
                return false;
            }

            HourComparator hourComparator = (HourComparator) obj;
            return hourComparator.hour == this.hour;
        }

    }

 */
}
