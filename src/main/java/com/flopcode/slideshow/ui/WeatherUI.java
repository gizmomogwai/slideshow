package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.SlideshowCanvas;
import com.flopcode.slideshow.WhiteboardForHandler;
import com.flopcode.slideshow.data.weather.WeatherIcons;
import com.flopcode.slideshow.processes.Weather;

import java.awt.Graphics2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.awt.Color.WHITE;

public class WeatherUI implements UI {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ForecastUI today;
    private final ForecastUI tomorrow;
    private final ForecastUI dayAfterTomorrow;
    private final SunriseSunsetUI sunriseSunset;
    private final CurrentConditionUI currentCondition;
    private final SlideshowCanvas.Fonts fonts;
    // private final ForecastGraphUI forecastGraph;

    private WeatherExtract weatherExtract;

    public WeatherUI(WhiteboardForHandler whiteboard, SlideshowCanvas.Fonts fonts) throws Exception {
        this.fonts = fonts;
        WeatherIcons icons = new WeatherIcons();

        sunriseSunset = new SunriseSunsetUI(whiteboard, icons);
        currentCondition = new CurrentConditionUI(whiteboard, icons);

        weatherExtract = new WeatherExtract(whiteboard);
        today = new ForecastUI(icons, () -> weatherExtract.getToday());
        tomorrow = new ForecastUI(icons, () -> weatherExtract.getTomorrow());
        dayAfterTomorrow = new ForecastUI(icons, () -> weatherExtract.getDayAfterTomorrow());

        // forecastGraph = new ForecastGraphUI(() -> weatherExtract.all);
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) {
        gfx.setFont(fonts.weather.font);
        gfx.setColor(WHITE);

        gfx.render(sunriseSunset, 0, 0);

        gfx.render(currentCondition, 0, 110);

        gfx.render(today, 0, gfx.fromTop(310));
        gfx.render(tomorrow, 0, gfx.fromTop(375));
        gfx.render(dayAfterTomorrow, 0, gfx.fromTop(440));
        //gfx.render(forecastGraph, 0, gfx.fromTop(600));
    }

    public static class Forecast_8_12_16 {
        Weather.Forecast _8;
        Weather.Forecast _12;
        Weather.Forecast _16;

        Forecast_8_12_16(List<Weather.Forecast> forecasts) {
            forecasts.stream().min(new HourComparator(8)).ifPresent(f -> _8 = f);
            forecasts.stream().min(new HourComparator(12)).ifPresent(f -> _12 = f);
            forecasts.stream().min(new HourComparator(16)).ifPresent(f -> _16 = f);
        }
    }

    private static class WeatherExtract {
        Forecast_8_12_16 today;
        Forecast_8_12_16 tomorrow;
        Forecast_8_12_16 dayAfterTomorrow;
        List<Weather.Forecast> all;

        WeatherExtract(WhiteboardForHandler whiteboard) {
            whiteboard.add("weatherInfo", (key, value) -> update((Weather.WeatherInfo) value));
        }

        public void update(Weather.WeatherInfo weatherInfo) {
            if (weatherInfo == null) {
                return;
            }
            LocalDate now = LocalDate.now();
            all = weatherInfo.forecasts.forecasts;
            today = new Forecast_8_12_16(getForecasts(weatherInfo, now));
            tomorrow = new Forecast_8_12_16(getForecasts(weatherInfo, now.plusDays(1)));
            dayAfterTomorrow = new Forecast_8_12_16(getForecasts(weatherInfo, now.plusDays(2)));
        }

        private List<Weather.Forecast> getForecasts(Weather.WeatherInfo weatherInfo, LocalDate date) {
            return weatherInfo.forecasts.forecasts.stream().filter(fc -> fc.time.toLocalDate().equals(date)).collect(Collectors.toList());
        }

        public Forecast_8_12_16 getDayAfterTomorrow() {
            return dayAfterTomorrow;
        }

        public Forecast_8_12_16 getTomorrow() {
            return tomorrow;
        }

        public Forecast_8_12_16 getToday() {
            return today;
        }
    }

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
}
