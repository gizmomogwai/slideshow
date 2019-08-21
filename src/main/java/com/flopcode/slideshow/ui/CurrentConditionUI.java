package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.WhiteboardForHandler;
import com.flopcode.slideshow.weather.Weather;
import com.flopcode.slideshow.weather.WeatherIcons;

import java.awt.Graphics2D;
import java.awt.Image;

class CurrentConditionUI implements UI {

    private final WeatherIcons icons;
    private Weather.WeatherInfo weatherInfo;

    CurrentConditionUI(WhiteboardForHandler whiteboard, WeatherIcons icons) {
        this.icons = icons;
        whiteboard.add("weatherInfo", (key, value) -> weatherInfo = (Weather.WeatherInfo) value);
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) throws Exception {
        if (weatherInfo != null) {
            Image now = icons.get(weatherInfo.condition.current);
            gfx.drawImage(now, gfx.fromRight(80 + now.getWidth(null)), 0);

            String s = "" + weatherInfo.temperature.current;
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 45);
            s = "" + weatherInfo.temperature.min;
            gfx.drawString(s, gfx.fromRight(50 + gfx.getStringBounds(s).getWidth()), 70);
            s = "" + weatherInfo.temperature.max;
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 70);
            s = weatherInfo.condition.current;
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 95);
            s = weatherInfo.condition.wind;
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 120);
        }
    }
}
