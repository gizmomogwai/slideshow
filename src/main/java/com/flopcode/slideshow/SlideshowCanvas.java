package com.flopcode.slideshow;

import com.flopcode.slideshow.database.DatabaseImage;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

import static java.awt.Color.BLACK;
import static java.awt.Image.SCALE_SMOOTH;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

class SlideshowCanvas extends Canvas {
    private final GeoLocationCache geoLocationCache;
    private final com.flopcode.slideshow.Font subtitles;
    private final com.flopcode.slideshow.Font calendar;
    private final com.flopcode.slideshow.Font today;
    private BufferStrategy buffers;
    private final Dimension screenSize;

    private SlideshowImage current;

    SlideshowCanvas(Dimension screenSize, GeoLocationCache geoLocationCache) throws Exception {
        this.geoLocationCache = geoLocationCache;
        setIgnoreRepaint(true);
        Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("FFF Tusj.ttf")));
        subtitles = new com.flopcode.slideshow.Font(this, font.deriveFont(48f));
        calendar = new com.flopcode.slideshow.Font(this, font.deriveFont(20f));
        today = new com.flopcode.slideshow.Font(this, font.deriveFont(Font.BOLD, 28f));
        this.screenSize = screenSize;
        current = new SlideshowImage(DatabaseImage.dummy(), new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY), subtitles, geoLocationCache);
    }

    private Graphics2D getGraphics2D(Dimension screenSize) {
        if (buffers == null) {
            createBufferStrategy(2);
            buffers = getBufferStrategy();
        }
        Graphics2D g = (Graphics2D) buffers.getDrawGraphics();
        g.setColor(BLACK);
        g.fillRect(0, 0, screenSize.width, screenSize.height);
        return g;
    }

    void transitionTo(DatabaseImage next) throws Exception {
        if (next == null) {
            throw new IllegalArgumentException();
        }
        SlideshowImage nextImage = new SlideshowImage(next, loadImage(next.getFile(), screenSize), subtitles, geoLocationCache);

        for (float i = 0; i < 1; i += 0.02) {
            Graphics2D g = getGraphics2D(screenSize);
            g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1 - i));
            current.render(g, screenSize);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, i));
            nextImage.render(g, screenSize);

            renderCalendar(g, screenSize, subtitles, calendar, today);

            g.dispose();
            buffers.show();
        }

        current.dispose();

        current = nextImage;
        Graphics2D g = getGraphics2D(screenSize);
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        current.render(g, screenSize);

        renderCalendar(g, screenSize, subtitles, calendar, today);

        g.dispose();
        buffers.show();
    }

    private void renderCalendar(Graphics2D g, Dimension screenSize, com.flopcode.slideshow.Font bigFont, com.flopcode.slideshow.Font smallFont, com.flopcode.slideshow.Font today) {
        Locale locale = Locale.ENGLISH;
        // background
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.setColor(new Color(0, 0, 0, 0.7f));
        g.fillRect(0, 0, screenSize.width, 120);

        // left side
        LocalDate now = LocalDate.now();
        g.setFont(bigFont.font);
        g.setColor(Color.WHITE);
        int year = now.getYear();
        Month month = now.getMonth();
        String dateFirstLine = now.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, locale) + " " + now.getDayOfMonth() + ".";
        g.drawString(dateFirstLine, 20, 50);
        String dateSecondLine = month.getDisplayName(TextStyle.SHORT_STANDALONE, locale) + " " + year;
        g.drawString(dateSecondLine, 20, 100);

        // dateline
        LocalDate current = LocalDate.of(year, month, 1);
        int i = 1;
        ColorScheme whiteOrBlack = new ColorScheme(Color.white, Color.black);
        ColorScheme redOrBlack = new ColorScheme(Color.red, Color.black);
        while (current.getMonthValue() == now.getMonthValue()) {
            ColorScheme cs = current.getDayOfWeek() == DayOfWeek.SUNDAY ? redOrBlack : whiteOrBlack;
            boolean renderingCurrentDay = current.equals(now);
            centerDay(g, current, 230, i++, renderingCurrentDay ? today : smallFont, renderingCurrentDay, cs);
            current = current.plusDays(1);
        }
    }

    private void centerDay(Graphics2D g, LocalDate date, int offset, int i, com.flopcode.slideshow.Font font, boolean currentDay, ColorScheme cs) {
        int deltaX = 40;
        g.setFont(font.font);

        String dayOfMonth = "" + date.getDayOfMonth();
        String dayOfWeek = ("" + date.getDayOfWeek()).substring(0, 1);

        Rectangle2D bounds = font.metrics.getStringBounds(dayOfMonth, g);
        Rectangle2D dayOfWeekBounds = font.metrics.getStringBounds(dayOfWeek, g);

        int width = (int) Math.max(bounds.getWidth(), dayOfWeekBounds.getWidth());
        int vOffset = 0;
        if (currentDay) {
            vOffset = 5; // compensate for bigger font
            int border = 3;
            g.setColor(new Color(1, 1, 1, 0.9f));
            int upperBorder = font.metrics.getMaxAscent();
            g.fillRect(offset + i * deltaX - width / 2 - border, 50 - upperBorder + vOffset, width + 2 * border, 25 + upperBorder + font.metrics.getMaxDescent());
            g.setColor(cs.onWhiteColor);
        } else {
            g.setColor(cs.normalColor);
        }
        g.drawString(dayOfMonth, offset + i * deltaX - ((int) bounds.getWidth() / 2), 50 + vOffset);
        g.drawString(dayOfWeek, offset + i * deltaX - ((int) dayOfWeekBounds.getWidth() / 2), 75 + vOffset);
    }

    private Rectangle2D grow(Rectangle2D r, int border) {
        return new Rectangle2D.Double(r.getX() - border, r.getY() - border, r.getWidth() + 2 * border, r.getHeight() + 2 * border);
    }

    private Image loadImage(File file, Dimension screenSize) throws Exception {
        try {
            BufferedImage originalImage = ImageIO.read(file);
            Image result = fit(originalImage, screenSize);
            originalImage.flush();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @SuppressWarnings("unused")
    private Image fit(BufferedImage image, Dimension size) {
        double factor = Math.min((double) size.width / (double) image.getWidth(),
                (double) size.height / (double) image.getHeight());
        return image.getScaledInstance((int) (image.getWidth() * factor), (int) (image.getHeight() * factor), SCALE_SMOOTH);
    }

    @SuppressWarnings("unused")
    private Image scaleToHeight(BufferedImage image, Dimension size) {
        return image.getScaledInstance(-1, size.height, SCALE_SMOOTH);
    }

    private static class ColorScheme {
        final Color normalColor;
        final Color onWhiteColor;

        ColorScheme(Color normalColor, Color onWhiteColor) {
            this.normalColor = normalColor;
            this.onWhiteColor = onWhiteColor;
        }
    }
}
