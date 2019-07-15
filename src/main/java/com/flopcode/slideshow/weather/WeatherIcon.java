package com.flopcode.slideshow.weather;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.util.HashMap;
import java.util.Objects;

import static java.awt.Image.SCALE_AREA_AVERAGING;

class WeatherIcon {

    HashMap<String, String> condition2Icon = new HashMap<String, String>() {
        {
            put("few clouds", "sun-and-clouds");
            put("overcast clouds", "sun-and-clouds");
            put("scattered clouds", "sun-and-clouds");
            put("broken clouds", "sun-and-clouds");
            put("light rain", "rain-clouds");
            put("shower rain", "rain-clouds");
            put("eclipse", "eclipse");
            put("moderate rain", "rain-clouds");
            put("heavy intensity rain", "stormrain-thunders");
            put("clear sky", "sun");
            put("mist", "moon-and-clouds");
            put("eclipse", "eclipse");
        }
    };
    HashMap<String, Image> imageCache = new HashMap<>();

    public Image get(String current) throws Exception {
        return get(current, 100);
    }

    public Image get(String current, int size) throws Exception {
        String key = current + size;

        if (imageCache.containsKey(key)) {
            return imageCache.get(key);
        }
        if (!condition2Icon.containsKey(current)) {
            current = "eclipse";
            System.out.println("WeatherIcon.get - cannot find " + current);
            throw new Exception("no icon for '" + current + "'");
        }

        String file = condition2Icon.get(current);
        Image image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("giallo/" + file + ".png"))).getScaledInstance(size, -1, SCALE_AREA_AVERAGING);
        imageCache.put(key, image);

        return imageCache.get(key);
    }
}
