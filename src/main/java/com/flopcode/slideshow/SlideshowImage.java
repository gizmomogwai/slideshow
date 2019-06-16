package com.flopcode.slideshow;

import com.flopcode.slideshow.database.DatabaseImage;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static javax.xml.xpath.XPathConstants.STRING;

public class SlideshowImage {
    final Image image;
    private final Font font;
    private final FontMetrics metrics;
    private final GeoLocationCache geoLocationCache;
    final DatabaseImage databaseImage;
    final String dateText;
    static private XPathExpression cityExpression;
    static private XPathExpression townExpression;
    static private XPathExpression hamletExpression;
    static private XPathExpression islandExpression;
    static private XPathExpression countyExpression;
    static private XPathExpression countryExpression;
    private final String locationText;
    private final String yearText;
    private final String title;

    public SlideshowImage(DatabaseImage databaseImage, Image image, Font font, FontMetrics metrics, GeoLocationCache geoLocationCache) {
        this.databaseImage = databaseImage;
        this.image = image;
        this.font = font;
        this.metrics = metrics;
        this.geoLocationCache = geoLocationCache;
        LocalDate now = LocalDate.now();
        this.dateText = textForDate(now, databaseImage);
        this.yearText = textForDeltaYears(now, databaseImage);
        this.locationText = textForLocation(databaseImage);
        this.title = Stream.of(dateText, yearText, locationText).filter((i) -> i != null).collect(Collectors.joining(" / "));
    }


    static {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            cityExpression = xpath.compile("reversegeocode/addressparts/city/text()");
            townExpression = xpath.compile("reversegeocode/addressparts/town/text()");
            hamletExpression = xpath.compile("reversegeocode/addressparts/hamlet/text()");
            islandExpression = xpath.compile("reversegeocode/addressparts/island/text()");
            countyExpression = xpath.compile("reversegeocode/addressparts/county/text()");
            countryExpression = xpath.compile("reversegeocode/addressparts/country_code/text()");
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    private String textForLocation(DatabaseImage databaseImage) {
        if (databaseImage.geoLocation == null) {
            return "guess!";
        }

        try {
            URL requestUrl = new URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + databaseImage.geoLocation.getLatitude() + "&lon=" + databaseImage.geoLocation.getLongitude() + "&zoom=10&addressdetails=1&format=xml");
            return geoLocationCache.get(requestUrl, (URL url) -> {
                System.out.println("url = " + url);
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent", "slideshow");
                connection.connect();

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(connection.getInputStream());
                prettyPrint(document);
                String country = ((String) countryExpression.evaluate(document, STRING)).toUpperCase();
                String detail = getFromXml(document, cityExpression, islandExpression, townExpression, hamletExpression, countyExpression);
                return Stream.of(detail, country).filter((i) -> i != null).collect(Collectors.joining(", "));
            });
        } catch (Exception e) {
            e.printStackTrace();
            return "in ?";
        }
    }

    private String getFromXml(Document document, XPathExpression... expressions) {
        for (XPathExpression expression : expressions) {
            try {
                String value = (String) expression.evaluate(document, STRING);
                if (value != null && !value.isEmpty()) {
                    return value;
                }
            } catch (XPathExpressionException e) {
            }
        }
        return null;
    }

    private void prettyPrint(Document document) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(document), new StreamResult(out));
        System.out.println(out.toString());

    }

    private String pluralize(String base, int count) {
        if (count > 1) {
            return base + "s";
        } else {
            return base;
        }
    }

    private String textForDeltaYears(LocalDate now, DatabaseImage databaseImage) {
        int yearDelta = now.getYear() - databaseImage.creationData.getYear();
        int monthDelta = now.getMonthValue() - databaseImage.creationData.getMonthValue();
        if (monthDelta < 0) {
            yearDelta -= 1;
            monthDelta += 12;
        }

        if (yearDelta == 0) {
            if (monthDelta == 0) {
                return "";
            } else {
                return monthDelta + " " + pluralize("month", monthDelta) + " ago";
            }
        } else {
            if (monthDelta == 0) {
                return yearDelta + " " + pluralize("year", yearDelta) + " ago";
            } else {
                return yearDelta + " " + pluralize("year", yearDelta) + " & " + monthDelta + " " + pluralize("month", monthDelta) + " ago";
            }
        }
    }

    private String textForDate(LocalDate now, DatabaseImage databaseImage) {
        if (databaseImage.creationData.getMonth() == now.getMonth()) {
            if (databaseImage.creationData.getDayOfMonth() == now.getDayOfMonth()) {
                return "THIS DAY!";
            }
        }
        return String.format("%d-%02d-%02d", databaseImage.creationData.getYear(),
                databaseImage.creationData.getMonth().getValue(),
                databaseImage.creationData.getDayOfMonth());
    }

    private String textForDate(LocalDate now) {
        if (databaseImage.creationData.getMonth() == now.getMonth()) {
            if (databaseImage.creationData.getDayOfMonth() == now.getDayOfMonth()) {
                int yearDelta = now.getYear() - databaseImage.creationData.getYear();
                if (yearDelta == 1) {
                    return "On this day last year";
                } else {
                    return "On this day " + yearDelta + " years ago";
                }
            }
        }
        return String.format("%d-%02d-%02d", databaseImage.creationData.getYear(),
                databaseImage.creationData.getMonth().getValue(),
                databaseImage.creationData.getDayOfMonth());
    }

    public void dispose() {
        image.flush();
    }

    public void render(Graphics2D graphics, Dimension screenSize) {
        center(graphics, screenSize, image);
        text(graphics, title, screenSize, screenSize.height - metrics.getMaxDescent() - 5);
        //text(graphics, locationText, screenSize, screenSize.height - metrics.getAscent() * 2 - 5);
    }

    private void center(Graphics2D graphics, Dimension screenSize, Image image) {
        graphics.drawImage(image, (screenSize.width - image.getWidth(null)) / 2, (screenSize.height - image.getHeight(null)) / 2, null);
    }

    private void text(Graphics2D graphics, String text, Dimension screenSize, int y) {
        graphics.setFont(font);
        graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);

        int x = metrics.getMaxDescent() + 5;
        graphics.setColor(Color.black);

        for (int i=-1;i<=1;++i) {
            for (int j=-1;j<=1;++j) {
                if (i != 0 && j != 0) {
                    graphics.drawString(text, x - i, y - j);
                }
            }
        }
        graphics.setColor(Color.white);
        graphics.drawString(text, x, y);
    }

}
