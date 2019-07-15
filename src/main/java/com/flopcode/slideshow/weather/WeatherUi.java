package com.flopcode.slideshow.weather;

import com.flopcode.slideshow.SlideshowCanvas;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.awt.Color.WHITE;

public class WeatherUi {
    private static final DateTimeFormatter SUN_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Image sunny;
    private final WeatherIcon icons;
    private WeatherExtract weatherExtract;

    public WeatherUi() throws Exception {
        icons = new WeatherIcon();
        sunny = icons.get("eclipse");
        weatherExtract = new WeatherExtract(null);
    }

    public void render(Graphics2D g, Dimension screenSize, SlideshowCanvas.Fonts fonts, Weather.WeatherInfo weatherInfo, int y) throws Exception {
        weatherExtract = weatherExtract.update(weatherInfo);

        drawImage(g, sunny, screenSize, -50, y);
        g.setFont(fonts.calendar.font);
        g.setColor(WHITE);
        drawString(g, SUN_DATE_TIME_FORMATTER.format(weatherInfo.sun.rise), screenSize, -8, y + 45);
        drawString(g, SUN_DATE_TIME_FORMATTER.format(weatherInfo.sun.set), screenSize, -8, y + 65);


        drawImage(g, icons.get(weatherInfo.condition.current), screenSize, -50, y + 80);
        drawString(g, "" + weatherInfo.temperature.current, screenSize, -8, y + 125);
        g.setFont(fonts.smallCalendar.font);
        g.setFont(fonts.smallCalendar.font);
        drawString(g, "" + weatherInfo.temperature.min, screenSize, -31, y + 140);
        drawString(g, "" + weatherInfo.temperature.max, screenSize, -8, y + 140);
        drawString(g, weatherInfo.condition.current, screenSize, -8, y + 160);

        renderForecastDay(g, screenSize, weatherExtract.today, y + 180);
        renderForecastDay(g, screenSize, weatherExtract.tomorrow, y + 220);
        renderForecastDay(g, screenSize, weatherExtract.dayAfterTomorrow, y + 260);
    }


    private void renderForecastDay(Graphics2D g, Dimension screenSize, Forecast_8_12_16 forecast, int y) throws Exception {
        drawImage(g, icons.get(forecast._8.symbol, 50), screenSize, -100, y);
        centerString(g, "" + forecast._8.temperature, screenSize, -125, y + 45);
        drawImage(g, icons.get(forecast._12.symbol, 50), screenSize, -50, y);
        centerString(g, "" + forecast._12.temperature, screenSize, -75, y + 45);
        drawImage(g, icons.get(forecast._16.symbol, 50), screenSize, -1, y);
        centerString(g, "" + forecast._16.temperature, screenSize, -26, y + 45);
    }

    private void centerString(Graphics2D g, String s, Dimension screenSize, int x, int y) {
        Rectangle2D size = g.getFontMetrics().getStringBounds(s, g);
        x = (x < 0) ? screenSize.width + x : x;
        g.drawString(s, (int) (x - size.getWidth() / 2), y);
    }

    private void drawImage(Graphics2D g, Image i, Dimension screenSize, int x, int y) {
        if (x < 0) {
            g.drawImage(i, screenSize.width - i.getWidth(null) + x, y, null);
        } else {
            g.drawImage(i, x, y, null);
        }
    }

    private void drawString(Graphics2D g, String s, Dimension screenSize, int x, int y) {
        if (x >= 0 && y >= 0) {
            g.drawString(s, x, y);
        }
        if (y < 0) {
            throw new RuntimeException("nyo");
        }
        if (x < 0) {
            Rectangle2D bounds = g.getFontMetrics().getStringBounds(s, g);
            g.drawString(s, (int) (screenSize.width - bounds.getWidth() + x), y);
        }
    }

    private static class Forecast_8_12_16 {
        Weather.Forecast _8;
        Weather.Forecast _12;
        Weather.Forecast _16;

        public Forecast_8_12_16(List<Weather.Forecast> forecasts) {
            _8 = forecasts.stream().min(new HourComparator(8)).get();
            _12 = forecasts.stream().min(new HourComparator(12)).get();
            _16 = forecasts.stream().min(new HourComparator(16)).get();
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
            return ((HourComparator) obj).hour == this.hour;
        }

    }
}
