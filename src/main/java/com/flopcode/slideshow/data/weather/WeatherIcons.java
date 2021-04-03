package com.flopcode.slideshow.data.weather;

import com.flopcode.slideshow.logger.Logger;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.util.HashMap;
import java.util.Objects;

import static java.awt.Image.SCALE_AREA_AVERAGING;

public class WeatherIcons {

    private HashMap<String, String> condition2Icon = new HashMap<String, String>() {
        {
            put("broken clouds", "sun-and-clouds");
            put("clear sky", "sun");
            put("eclipse", "eclipse");
            put("few clouds", "sun-and-clouds");
            put("heavy intensity rain", "stormrain-thunders");
            put("light rain", "rain-clouds");
            put("light snow", "snow-clouds");
            put("mist", "moon-and-clouds");
            put("moderate rain", "rain-clouds");
            put("overcast clouds", "sun-and-clouds");
            put("rain and snow", "snow-clouds");
            put("scattered clouds", "sun-and-clouds");
            put("shower rain", "rain-clouds");
            put("very heavy rain", "stormrain-thunders");
        }
    };

    private HashMap<String, Image> imageCache = new HashMap<>();
    private final Logger logger;

    public WeatherIcons(Logger logger) {
        this.logger = logger;
    }

    public Image get(String current) throws Exception {
        return get(current, 100);
    }

    public Image get(String current, int size) throws Exception {
        String key = current + size;

        if (imageCache.containsKey(key)) {
            return imageCache.get(key);
        }
        if (!condition2Icon.containsKey(current)) {
            logger.e("WeatherIcon.get - cannot find " + current + " falling back to eclipse");
            current = "eclipse";
        }

        String file = condition2Icon.get(current);
        Image image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("giallo/" + file + ".png"))).getScaledInstance(size, -1, SCALE_AREA_AVERAGING);
        if (image == null) {
            logger.e("Cannot get weather icon for condition '" + current + "'");
        }
        imageCache.put(key, image);

        return imageCache.get(key);
    }
}
