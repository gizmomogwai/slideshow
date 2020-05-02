package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.WhiteboardForHandler;
import com.flopcode.slideshow.data.weather.WeatherIcons;
import com.flopcode.slideshow.processes.Weather;

import java.awt.Graphics2D;
import java.awt.Image;

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
            // icon
            Image now = icons.get(weatherInfo.dailies.get(0).weather.get(0).description);
            gfx.drawImage(now, gfx.fromRight(80 + now.getWidth(null)), 0);

            // current temperature
            String s = "" + Math.round(weatherInfo.current.temperature);
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45);

            Weather.TemperatureForecast temperatureForecast = weatherInfo.dailies.get(0).temperatureForecast;

            // daily minimum
            s = "" + Math.round(temperatureForecast.min);
            gfx.drawString(s, gfx.fromRight(50 + gfx.getStringBounds(s).getWidth()), 45 + LINE_DISTANCE);

            // daily maximum
            s = "" + Math.round(temperatureForecast.max);
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45 + LINE_DISTANCE);

            // weather description (e.g. rainy)
            s = "" + weatherInfo.current.weather.get(0).description;
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45 + 2 * LINE_DISTANCE);

            // windspeed
            s = String.format("%d km/h", Math.round((weatherInfo.current.windSpeed * 3600) / 1000));
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45 + 3 * LINE_DISTANCE);
        }
    }
}
