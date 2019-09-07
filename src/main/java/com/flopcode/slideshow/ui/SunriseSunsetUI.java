/*
 * Copyright (c) 2019 E.S.R.Labs. All rights reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of E.S.R.Labs and its suppliers, if any.
 * The intellectual and technical concepts contained herein are
 * proprietary to E.S.R.Labs and its suppliers and may be covered
 * by German and Foreign Patents, patents in process, and are protected
 * by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from E.S.R.Labs.
 */
package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.WhiteboardForHandler;
import com.flopcode.slideshow.data.weather.WeatherIcons;
import com.flopcode.slideshow.processes.Weather;

import java.awt.Graphics2D;
import java.awt.Image;

class SunriseSunsetUI implements UI {

    private final Image sunriseSunsetImage;
    private Weather.WeatherInfo weatherInfo;

    SunriseSunsetUI(WhiteboardForHandler whiteboardForHandler, WeatherIcons icons) throws Exception {
        sunriseSunsetImage = icons.get("eclipse");
        whiteboardForHandler.add("weatherInfo", (key, value) -> weatherInfo = (Weather.WeatherInfo) value);
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) {
        if (weatherInfo != null) {
            gfx.drawImage(sunriseSunsetImage, gfx.fromRight(80 + sunriseSunsetImage.getWidth(null)), 0);
            String s = WeatherUI.TIME_FORMATTER.format(weatherInfo.sun.rise);
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 41);
            s = WeatherUI.TIME_FORMATTER.format(weatherInfo.sun.set);
            gfx.drawString(s, gfx.fromRight(8 + gfx.getStringBounds(s).getWidth()), 75);
        }
    }
}
