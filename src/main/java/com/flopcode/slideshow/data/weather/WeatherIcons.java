package com.flopcode.slideshow.data.weather;

import com.flopcode.slideshow.logger.Logger;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import static java.awt.Image.SCALE_AREA_AVERAGING;

public class WeatherIcons {

    private HashMap<String, String> condition2Icon = new HashMap<>();
    private HashMap<String, Image> imageCache = new HashMap<>();
    private final Logger logger;

    public WeatherIcons(Logger logger) {
        this.logger = logger;
        condition2Icon.put("broken clouds", "sun-and-clouds");
        condition2Icon.put("clear sky", "sun");
        condition2Icon.put("eclipse", "eclipse");
        condition2Icon.put("few clouds", "sun-and-clouds");
        condition2Icon.put("heavy intensity rain", "stormrain-thunders");
        condition2Icon.put("light rain", "rain-clouds");
        condition2Icon.put("light snow", "snow-clouds");
        condition2Icon.put("mist", "moon-and-clouds");
        condition2Icon.put("moderate rain", "rain-clouds");
        condition2Icon.put("overcast clouds", "sun-and-clouds");
        condition2Icon.put("rain and snow", "snow-clouds");
        condition2Icon.put("scattered clouds", "sun-and-clouds");
        condition2Icon.put("shower rain", "rain-clouds");
        condition2Icon.put("snow", "snow-clouds");
        condition2Icon.put("very heavy rain", "stormrain-thunders");
    }

    public Image get(String current) throws IOException {
        return get(current, 100);
    }

    public Image get(String current, int size) throws IOException {
        String key = current + size;

        if (imageCache.containsKey(key)) {
            return imageCache.get(key);
        }
        if (!condition2Icon.containsKey(current)) {
            logger.e("WeatherIcon.get - cannot find '" + current + "' falling back to 'eclipse'");
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
