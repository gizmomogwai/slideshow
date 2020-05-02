package com.flopcode.slideshow.processes;

import com.flopcode.slideshow.Whiteboard;
import com.flopcode.slideshow.logger.Logger;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.time.Duration.ofSeconds;

public class Weather extends Thread {

    private static final String APP_ID = "9d8617eb77cba019774d79121d412a0e";
    private final Whiteboard whiteboard;
    private final Logger logger;

    public Weather(Logger logger, Whiteboard whiteboard) {
        this.logger = logger;
        this.whiteboard = whiteboard;
        start();
    }

    private static ZonedDateTime parseDateTime(String s) {
        OffsetDateTime dateTime = OffsetDateTime.parse(s + "Z");
        return dateTime.atZoneSameInstant(ZoneId.systemDefault());
    }

    public void run() {
        while (true) {
            try {
                updateWeather();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sleep(ofSeconds(15).toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateWeather() throws Exception {
        // Carola-Neher-Str 10, 48.0878521,11.5414829
        String requestUrl =
                format("https://api.openweathermap.org/data/2.5/onecall?lat=48.0878521&lon=11.5414829&appid=%s&units=metric", APP_ID);
        whiteboard.set("weatherInfo", get(requestUrl));
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
                LocalDateTime local = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
                return local;
            } else {
                return null;
            }
        }
    }

    private WeatherInfo get(String s) throws IOException, ParserConfigurationException, SAXException {
        URL url = new URL(s);
        logger.d("Weather.getDocument - " + url);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "slideshow");
        connection.connect();

        WeatherInfo weatherInfo = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new UnixUTCTimestampAdapter())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
                .fromJson(
                        new InputStreamReader(
                                connection.getInputStream(),
                                "UTF-8"),
                        WeatherInfo.class);
        return weatherInfo;
    }

    public static class Current {
        @SerializedName("temp")
        public float temperature;
        public float windSpeed;
        public List<WeatherDescription> weather;
    }

    public static class WeatherInfo {
        @SerializedName("current")
        public Current current;
        @SerializedName("daily")
        public List<Forecast> dailies;
        public List<WeatherDescription> weather;
    }

    public static class Temperature {
        private static final XPath xpath;

        static {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            xpath = xPathfactory.newXPath();
        }

        public final int current;
        public final int min;
        public final int max;

        Temperature(Document d) throws Exception {
            this(Math.round(Float.parseFloat(xpath.compile("current/temperature/@value").evaluate(d))), Math.round(Float.parseFloat(xpath.compile("current/temperature/@min").evaluate(d))), Math.round(Float.parseFloat(xpath.compile("current/temperature/@max").evaluate(d))));
        }

        Temperature(int current, int min, int max) {
            this.current = current;
            this.min = min;
            this.max = max;
        }
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
        public List<WeatherDescription> weather;
    }

    public static class WeatherDescription {
        public String description;
        String icon;
        int id;
    }
}
