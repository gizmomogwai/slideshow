package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.WhiteboardForHandler;
import com.flopcode.slideshow.data.weather.WeatherIcons;
import com.flopcode.slideshow.processes.Weather;

import java.awt.Graphics2D;
import java.util.Optional;

class CurrentConditionUI implements UI {

    public static final int LINE_DISTANCE = 34;
    private final WeatherIcons icons;
    private Weather.WeatherInfo weatherInfo;

    CurrentConditionUI(WhiteboardForHandler whiteboard, WeatherIcons icons) {
        this.icons = icons;
        whiteboard.add("weatherInfo", (key, value) -> weatherInfo = (Weather.WeatherInfo) value);
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) throws Exception {
        if (weatherInfo != null) {

            String weatherDescriptionToday = weatherInfo.dailies.get(0).weather.get(0).description;
            Weather.TemperatureForecast temperatureForecast = weatherInfo.dailies.get(0).temperatureForecast;
            float currentTemperature = weatherInfo.current.temperature;
            float minTemperature = temperatureForecast.min;
            float maxTemperature = temperatureForecast.max;
            float windSpeed = (weatherInfo.current.windSpeed * 3600) / 1000;
            int windDegree = weatherInfo.current.windDegree;

            ForecastUI.renderWeatherConditions(gfx,
                    icons,
                    weatherDescriptionToday,
                    Optional.of(currentTemperature),
                    minTemperature,
                    maxTemperature,
                    windSpeed,
                    windDegree);
        }
    }


    public static class WindRange {
        public final double from;
        public final double to;
        public final String name;

        private boolean includes(int x) {
            return from <= x && x <= to;
        }

        private static final WindRange[] RANGES = new WindRange[] {
                new WindRange(337.5, 360.0, "N"),
                new WindRange(0.0, 22.5, "N"),
                new WindRange(22.5, 67.5, "NE"),
                new WindRange(67.5, 112.5, "E"),
                new WindRange(112.5, 157.5, "SE"),
                new WindRange(157.5, 202.5, "S"),
                new WindRange(202.5, 247.5, "SW"),
                new WindRange(247.5, 292.5, "W"),
                new WindRange(292.5, 337.5, "NW"),
        };

        public static WindRange fromDegree(int degree) {
            for (WindRange range : RANGES) {
                if (range.includes(degree)) {
                    return range;
                }
            }
            throw new IndexOutOfBoundsException("Cannot process " + degree);
        }

        private WindRange(double from, double to, String name) {
            this.from = from;
            this.to = to;
            this.name = name;
        }
    }
}
