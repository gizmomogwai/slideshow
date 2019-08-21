package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.weather.WeatherIcons;

import java.awt.Graphics2D;
import java.util.function.Supplier;

class ForecastUI implements UI {

    private final WeatherIcons icons;
    private final Supplier<WeatherUI.Forecast_8_12_16> getter;

    ForecastUI(WeatherIcons icons, Supplier<WeatherUI.Forecast_8_12_16> getter) {
        this.icons = icons;
        this.getter = getter;
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) throws Exception {
        WeatherUI.Forecast_8_12_16 forecast = getter.get();
        if (forecast != null) {
            gfx.drawImage(icons.get(forecast._8.symbol, 50), gfx.fromRight(150), 0);
            gfx.drawImage(icons.get(forecast._12.symbol, 50), gfx.fromRight(100), 0);
            gfx.drawImage(icons.get(forecast._16.symbol, 50), gfx.fromRight(50), 0);
            gfx.centerString("" + forecast._8.temperature, gfx.fromRight(125), 60);
            gfx.centerString("" + forecast._12.temperature, gfx.fromRight(75), 60);
            gfx.centerString("" + forecast._16.temperature, gfx.fromRight(25), 60);
        }
    }
}
