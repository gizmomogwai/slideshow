package com.flopcode.slideshow.weather;

import com.flopcode.slideshow.Whiteboard;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;
import static java.lang.String.format;
import static java.time.Duration.ofMinutes;

public class Weather extends Thread {

    private static final String APP_ID = "9d8617eb77cba019774d79121d412a0e";
    private final Whiteboard whiteboard;

    public Weather(Whiteboard whiteboard) {
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
                sleep(ofMinutes(1).toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateWeather() throws Exception {
        Document forecast = getDocument("http://api.openweathermap.org/data/2.5/forecast?q=Munich,de&appid=" + APP_ID + "&mode=xml&units=metric");
        prettyPrint(forecast);

        Document current = getDocument("http://api.openweathermap.org/data/2.5/weather?q=Munich,de&appid=" + APP_ID + "&mode=xml&units=metric");
        prettyPrint(current);

        WeatherInfo weatherInfo = new WeatherInfo(current, forecast);

        whiteboard.set("weatherInfo", weatherInfo);
    }

    private Document getDocument(String s) throws IOException, ParserConfigurationException, SAXException {
        URL url = new URL(s);
        System.out.println("Weather.getDocument - " + url);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "slideshow");
        connection.connect();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(connection.getInputStream());
    }

    private void prettyPrint(Document document) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(document), new StreamResult(out));
        System.out.println(out.toString());
    }

    public static class WeatherInfo {
        public final Temperature temperature;
        public final Forecasts forecasts;
        public final Sun sun;
        public final Condition condition;

        WeatherInfo(Document current, Document forecast) throws Exception {
            this.sun = new Sun(current);
            this.temperature = new Temperature(current);
            this.forecasts = new Forecasts(forecast);
            this.condition = new Condition(current);
        }
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

    public static class Sun {
        private static final XPathExpression riseQuery;
        private static final XPathExpression setQuery;

        static {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            try {
                riseQuery = xpath.compile("current/city/sun/@rise");
                setQuery = xpath.compile("current/city/sun/@set");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public ZonedDateTime rise;
        public ZonedDateTime set;

        Sun(Document d) throws Exception {
            rise = parseDateTime(riseQuery.evaluate(d));
            set = parseDateTime(setQuery.evaluate(d));
        }

    }

    public static class Condition {
        private static final XPathExpression currentXPath;
        private static final XPathExpression windXPath;

        static {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            try {
                currentXPath = xpath.compile("current/weather/@value");
                windXPath = xpath.compile("current/wind/speed/@value");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public final String current;
        public final String wind;

        Condition(Document d) throws Exception {
            this.current = currentXPath.evaluate(d);
            this.wind = format("%.1f", parseDouble(windXPath.evaluate(d)) * 3.6) + " km/h";
        }
    }

    public static class Forecast {
        public final String symbol;
        public final ZonedDateTime time;
        public final int temperature;

        Forecast(ZonedDateTime time, String symbol, int temperature) {
            this.time = time;
            this.symbol = symbol;
            this.temperature = temperature;
        }
    }

    public static class Forecasts {
        public final List<Forecast> forecasts;

        Forecasts(Document d) throws Exception {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            NodeList forecastNodes = (NodeList) xpath.compile("weatherdata/forecast/time").evaluate(d, XPathConstants.NODESET);
            XPathExpression temperature = xpath.compile("temperature/@value");
            XPathExpression symbol = xpath.compile("symbol/@name");
            forecasts = new ArrayList<>();
            for (int i = 0; i < forecastNodes.getLength(); ++i) {
                Node forecast = forecastNodes.item(i);
                ZonedDateTime date = parseDateTime(forecast.getAttributes().getNamedItem("from").getTextContent());
                forecasts.add(new Forecast(date, symbol.evaluate(forecast), Math.round(Float.parseFloat(temperature.evaluate(forecast)))));
            }
        }

    }
}