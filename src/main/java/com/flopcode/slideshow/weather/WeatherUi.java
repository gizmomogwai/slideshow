package com.flopcode.slideshow.weather;

import com.flopcode.slideshow.Gfx;
import com.flopcode.slideshow.SlideshowCanvas;
import com.flopcode.slideshow.UI;

import java.awt.Graphics2D;
import java.awt.Image;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.awt.Color.WHITE;

public class WeatherUi {
    private static final DateTimeFormatter SUN_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Forecast today;
    private final Forecast tomorrow;
    private final Forecast dayAfterTomorrow;
    private final SunriseSunset sunriseSunset;
    private final CurrentCondition currentCondition;
    private WeatherExtract weatherExtract;

    public WeatherUi() throws Exception {
        WeatherIcon icons = new WeatherIcon();
        weatherExtract = new WeatherExtract(null);
        today = new Forecast(icons, () -> weatherExtract.today);
        tomorrow = new Forecast(icons, () -> weatherExtract.tomorrow);
        dayAfterTomorrow = new Forecast(icons, () -> weatherExtract.dayAfterTomorrow);
        sunriseSunset = new SunriseSunset(icons);
        currentCondition = new CurrentCondition(icons);
    }

    static class CurrentCondition implements UI {

        private final WeatherIcon icons;
        private Weather.WeatherInfo weatherInfo;

        CurrentCondition(WeatherIcon icons) {
            this.icons = icons;
        }

        @Override
        public void render(Gfx gfx, Graphics2D g) throws Exception {
            Image now = icons.get(weatherInfo.condition.current);
            gfx.drawImage(now, gfx.fromRight(70 + now.getWidth(null)), 0);

            String s = "" + weatherInfo.temperature.current;
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45);
            s = "" + weatherInfo.temperature.min;
            gfx.drawString(s, gfx.fromRight(45 + gfx.getStringBounds(s).getWidth()), 60);
            s = "" + weatherInfo.temperature.max;
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 60);
            s = weatherInfo.condition.current;
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 80);
            s = weatherInfo.condition.wind;
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 100);
        }

        public void update(Weather.WeatherInfo current) {
            this.weatherInfo = current;
        }
    }

    static class SunriseSunset implements UI {

        private final Image sunriseSunsetImage;
        private Weather.WeatherInfo weatherInfo;

        SunriseSunset(WeatherIcon icons) throws Exception {
            sunriseSunsetImage = icons.get("eclipse");
        }

        @Override
        public void render(Gfx gfx, Graphics2D g) {
            gfx.drawImage(sunriseSunsetImage, gfx.fromRight(70 + sunriseSunsetImage.getWidth(null)), 0);
            String s = SUN_DATE_TIME_FORMATTER.format(weatherInfo.sun.rise);
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45);
            s = SUN_DATE_TIME_FORMATTER.format(weatherInfo.sun.set);
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 65);
        }

        public void update(Weather.WeatherInfo weatherInfo) {
            this.weatherInfo = weatherInfo;
        }
    }

    public void render(Gfx gfx, SlideshowCanvas.Fonts fonts, Weather.WeatherInfo weatherInfo, int y) {
        weatherExtract = weatherExtract.update(weatherInfo);
        sunriseSunset.update(weatherInfo);
        currentCondition.update(weatherInfo);

        gfx.setFont(fonts.calendar.font);
        gfx.setColor(WHITE);

        gfx.render(0, y, sunriseSunset);

        gfx.render(0, y + 80, currentCondition);
        gfx.render(0, gfx.fromTop(y + 200), today);
        gfx.render(0, gfx.fromTop(y + 250), tomorrow);
        gfx.render(0, gfx.fromTop(y + 300), dayAfterTomorrow);
    }

    static class Forecast implements UI {

        private final WeatherIcon icons;
        private final Supplier<Forecast_8_12_16> getter;

        Forecast(WeatherIcon icons, Supplier<Forecast_8_12_16> getter) {
            this.icons = icons;
            this.getter = getter;
        }

        @Override
        public void render(Gfx gfx, Graphics2D g) throws Exception {
            Forecast_8_12_16 forecast = getter.get();
            gfx.drawImage(icons.get(forecast._8.symbol, 50), gfx.fromRight(150), 0);
            gfx.drawImage(icons.get(forecast._12.symbol, 50), gfx.fromRight(100), 0);
            gfx.drawImage(icons.get(forecast._16.symbol, 50), gfx.fromRight(50), 0);
            gfx.centerString("" + forecast._8.temperature, gfx.fromRight(125), 50);
            gfx.centerString("" + forecast._12.temperature, gfx.fromRight(75), 50);
            gfx.centerString("" + forecast._16.temperature, gfx.fromRight(25), 50);
        }
    }

    private static class Forecast_8_12_16 {
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
        Weather.WeatherInfo info;

        Forecast_8_12_16 today;
        Forecast_8_12_16 tomorrow;
        Forecast_8_12_16 dayAfterTomorrow;

        WeatherExtract(Weather.WeatherInfo info) {
            this.info = info;
            if (info == null) {
                return;
            }
            LocalDate now = LocalDate.now();
            today = new Forecast_8_12_16(getForecasts(info, now));
            tomorrow = new Forecast_8_12_16(getForecasts(info, now.plusDays(1)));
            dayAfterTomorrow = new Forecast_8_12_16(getForecasts(info, now.plusDays(2)));
        }

        public WeatherExtract update(Weather.WeatherInfo weatherInfo) {
            if (info == weatherInfo) {
                return this;
            }
            return new WeatherExtract(weatherInfo);
        }

        private List<Weather.Forecast> getForecasts(Weather.WeatherInfo weatherInfo, LocalDate date) {
            return weatherInfo.forecasts.forecasts.stream().filter(fc -> fc.time.toLocalDate().equals(date)).collect(Collectors.toList());
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
