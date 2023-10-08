package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.WhiteboardForHandler;
import com.flopcode.slideshow.data.weather.WeatherIcons;
import com.flopcode.slideshow.processes.Weather;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Optional;
import java.util.function.Function;

class ForecastUI implements UI {

    private final WeatherIcons icons;
    Weather.Forecast forecast;

    ForecastUI(WhiteboardForHandler whiteboard, WeatherIcons icons, Function<Weather.WeatherInfo, Weather.Forecast> transform) {
        this.icons = icons;
        whiteboard.add("weatherInfo", (key, value) -> forecast = transform.apply((Weather.WeatherInfo) value));
    }

    public static void renderWeatherConditions(Gfx gfx, WeatherIcons icons, String weatherDescriptionToday, Optional<Float> currentTemperature, float minTemperature, float maxTemperature, float windSpeedF, int windDegreeI) throws Exception {
        // icon
        Image now = icons.get(weatherDescriptionToday);
        gfx.drawImage(now, gfx.fromRight(80 + now.getWidth(null)), 0);

        // current temperature
        if (currentTemperature.isPresent()) {
            String s = "" + Math.round(currentTemperature.get());
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45);
        }

        // min max temperatures
        String minMax = String.format("%d-%d", Math.round(minTemperature), Math.round(maxTemperature));
        gfx.drawString(minMax, gfx.fromRight(8 + gfx.getStringBounds(minMax).getWidth()), 45 + CurrentConditionUI.LINE_DISTANCE);

        // weather description (e.g. rainy)
        {
            String s = weatherDescriptionToday;
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45 + 2 * CurrentConditionUI.LINE_DISTANCE);
        }
        {
            // wind
            String windSpeed = String.format("%d km/h", Math.round(windSpeedF));
            String windDegree = CurrentConditionUI.WindRange.fromDegree(windDegreeI).name;
            gfx.drawString(windDegree, gfx.fromRight(8 + gfx.getStringBounds(windDegree).getWidth()), 45 + 3 * CurrentConditionUI.LINE_DISTANCE);
            gfx.drawString(windSpeed, gfx.fromRight(2 * 8 + gfx.getStringBounds(windSpeed).getWidth() + gfx.getStringBounds(windDegree).getWidth()), 45 + 3 * CurrentConditionUI.LINE_DISTANCE);
        }
    }


    @Override
    public void render(Gfx gfx, Graphics2D g) throws Exception {
        if (forecast != null) {
            Weather.TemperatureForecast temperatureForecast = forecast.temperatureForecast;
            renderWeatherConditions(gfx,
                    icons,
                    forecast.weather.get(0).description,
                    Optional.empty(),
                    temperatureForecast.min,
                    temperatureForecast.max,
                    (forecast.windSpeed * 3600) / 1000,
                    forecast.windDegree
            );
        }
    }
}
