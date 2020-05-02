package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.WhiteboardForHandler;
import com.flopcode.slideshow.data.weather.WeatherIcons;
import com.flopcode.slideshow.processes.Weather;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.function.Function;

class ForecastUI implements UI {

    private final WeatherIcons icons;
    Weather.Forecast forecast;

    ForecastUI(WhiteboardForHandler whiteboard, WeatherIcons icons, Function<Weather.WeatherInfo, Weather.Forecast> transform) {
        this.icons = icons;
        whiteboard.add("weatherInfo", (key, value) -> forecast = transform.apply((Weather.WeatherInfo) value));
    }


    @Override
    public void render(Gfx gfx, Graphics2D g) throws Exception {
        if (forecast != null) {
            final int LINE_DISTANCE = 34;

            // icon
            Image now = icons.get(forecast.weather.get(0).description);
            gfx.drawImage(now, gfx.fromRight(80 + now.getWidth(null)), 0);

            // daily minimum
            Weather.TemperatureForecast temperatureForecast = forecast.temperatureForecast;
            String s = "" + Math.round(temperatureForecast.min);
            gfx.drawString(s, gfx.fromRight(50 + gfx.getStringBounds(s).getWidth()), 45 + LINE_DISTANCE);

            // daily maximum
            s = "" + Math.round(temperatureForecast.max);
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45 + LINE_DISTANCE);

            // weather description (e.g. rainy)
            s = "" + forecast.weather.get(0).description;
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45 + 2 * LINE_DISTANCE);

            // windspeed
            s = String.format("%d km/h", Math.round((forecast.windSpeed * 3600) / 1000));
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45 + 3 * LINE_DISTANCE);
        }
    }
}
