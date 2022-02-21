package com.flopcode.slideshow.processes;

import com.flopcode.slideshow.Constants;
import com.flopcode.slideshow.Whiteboard;
import com.flopcode.slideshow.logger.Logger;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.google.common.primitives.Floats.max;
import static com.google.common.primitives.Floats.min;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Weather extends Thread {

    private final Whiteboard whiteboard;
    private final Logger logger;
    private final MinMax minMax = new MinMax();
    private final String[] latLon;
    private final String appToken;

    public Weather(Logger logger, Whiteboard whiteboard, String latLon, String appToken) {
        this.logger = logger;
        this.whiteboard = whiteboard;
        this.latLon = latLon.split(",");
        this.appToken = appToken;
        if (this.latLon.length != 2) {
            throw new IllegalArgumentException("Cannot parse latlon: '" + latLon + "'");
        }
        start();
    }

    private static class MinMax {
        LocalDateTime today = LocalDateTime.now();
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        public void update(WeatherInfo info) {
            if (!info.current.timestamp.toLocalDate().equals(today.toLocalDate())) {
                // new day new luck, new min, new max
                today = info.current.timestamp;
                min = Float.MAX_VALUE;
                max = Float.MIN_VALUE;
            }

            min = min(info.dailies.get(0).temperatureForecast.min, info.current.temperature, min);
            max = max(info.dailies.get(0).temperatureForecast.max, info.current.temperature, max);

            info.dailies.get(0).temperatureForecast.min = min;
            info.dailies.get(0).temperatureForecast.max = max;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                updateWeather();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sleep(Constants.WEATHER_POLL_CYCLE.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    private void updateWeather() throws IOException {
        // Carola-Neher-Str 10, 48.0878521,11.5414829
        // Seewaldweg 15, 47.678710, 11.186446
        String requestUrl =
                format("https://api.openweathermap.org/data/2.5/onecall?lat=%s&lon=%s&units=metric&appid=%s",
                        latLon[0], latLon[1], appToken);
        WeatherInfo forecastWeatherInfo = get(requestUrl, WeatherInfo.class);
        minMax.update(forecastWeatherInfo);

        whiteboard.set("weatherInfo", forecastWeatherInfo);
    }

    public static class UnixUTCTimestampAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.atZone(ZoneId.systemDefault()).toEpochSecond());
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in != null) {
                long epochMilli = in.nextLong();
                Instant instant = Instant.ofEpochMilli(epochMilli * 1000);
                return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
            } else {
                return null;
            }
        }
    }

private <T> T get(String s, Class<T> clazz) throws IOException {
        URL url = new URL(s);
        logger.d("Weather.getDocument - " + url);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "slideshow");
        connection.connect();

        T result = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new UnixUTCTimestampAdapter())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
                .fromJson(
                        new InputStreamReader(
                                connection.getInputStream(),
                                UTF_8),
                        clazz);

        logger.i("Got weatherInfo: " + new GsonBuilder().setPrettyPrinting().create().toJson(result));
        return result;
    }

    public static class Current {
        @SerializedName("dt")
        LocalDateTime timestamp;
        @SerializedName("temp")
        public float temperature;
        public float windSpeed;
        @SerializedName("wind_deg")
        public int windDegree;
        public List<WeatherDescription> weather;
    }

    public static class WeatherInfo {
        public Current current;
        @SerializedName("daily")
        public List<Forecast> dailies;
        public List<WeatherDescription> weather;
    }

    public static class TemperatureForecast {
        public float min;
        public float max;
    }

    public static class Forecast {
        public LocalDateTime sunrise;
        public LocalDateTime sunset;
        @SerializedName("temp")
        public TemperatureForecast temperatureForecast;
        public float windSpeed;
        @SerializedName("wind_deg")
        public int windDegree;
        public List<WeatherDescription> weather;
    }

    public static class WeatherDescription {
        public String description;
        String icon;
        int id;
    }
}
