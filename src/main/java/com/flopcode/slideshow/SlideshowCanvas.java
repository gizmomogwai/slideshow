package com.flopcode.slideshow;

import com.flopcode.slideshow.database.DatabaseImage;
import com.google.common.collect.Sets;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

import static java.awt.AlphaComposite.SRC_OVER;
import static java.awt.Color.BLACK;
import static java.awt.Font.TRUETYPE_FONT;
import static java.awt.Font.createFont;
import static java.awt.Image.SCALE_SMOOTH;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.format.TextStyle.SHORT_STANDALONE;

class SlideshowCanvas extends Canvas {
    private final GeoLocationCache geoLocationCache;
    private final PublicHolidays publicHolidays;
    private final Dimension screenSize;
    private Fonts fonts;
    private BufferStrategy buffers;
    private SlideshowImage current;

    SlideshowCanvas(Dimension screenSize, GeoLocationCache geoLocationCache) throws Exception {
        this.geoLocationCache = geoLocationCache;
        setIgnoreRepaint(true);
        fonts = new Fonts(this, createFont(TRUETYPE_FONT, Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("FFF Tusj.ttf"))));
        this.screenSize = screenSize;
        current = new SlideshowImage(DatabaseImage.dummy(), new BufferedImage(1, 1, TYPE_BYTE_GRAY), fonts.subtitles, geoLocationCache);
        publicHolidays = new PublicHolidays();
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
        SlideshowImage nextImage = new SlideshowImage(next, loadImage(next.getFile(), screenSize), fonts.subtitles, geoLocationCache);

        for (float i = 0; i < 1; i += 0.02) {
            Graphics2D g = getGraphics2D(screenSize);
            g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(SRC_OVER, 1 - i));
            current.render(g, screenSize);
            g.setComposite(AlphaComposite.getInstance(SRC_OVER, i));
            nextImage.render(g, screenSize);

            renderCalendar(g, screenSize, fonts);

            g.dispose();
            buffers.show();
        }

        current.dispose();

        current = nextImage;
        Graphics2D g = getGraphics2D(screenSize);
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        current.render(g, screenSize);

        renderCalendar(g, screenSize, fonts);

        g.dispose();
        buffers.show();
    }

    private void renderCalendar(Graphics2D g, Dimension screenSize, Fonts fonts) {
        Locale locale = Locale.ENGLISH;
        LocalDate now = LocalDate.now();

        CalendarBackground.render(g, screenSize);
        CalendarDate.render(g, fonts.subtitles, now, locale);
        CalendarLine.render(g, 230, fonts.calendar, now, publicHolidays);
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

    static class Fonts {
        private final com.flopcode.slideshow.Font subtitles;
        private final com.flopcode.slideshow.Font calendar;

        Fonts(Component c, Font baseFont) {
            this.subtitles = new com.flopcode.slideshow.Font(c, baseFont.deriveFont(48f));
            this.calendar = new com.flopcode.slideshow.Font(c, baseFont.deriveFont(20f));
        }
    }

    private static class ColorScheme {
        final Color normalColor;

        ColorScheme(Color normalColor) {
            this.normalColor = normalColor;
        }
    }

    private static class PublicHolidays {
        HashSet<LocalDate> publicHolidays = Sets.newHashSet(
                // 2019
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 1, 6),
                LocalDate.of(2019, 4, 19),
                LocalDate.of(2019, 4, 21),
                LocalDate.of(2019, 4, 22),
                LocalDate.of(2019, 5, 1),
                LocalDate.of(2019, 5, 30),
                LocalDate.of(2019, 6, 9),
                LocalDate.of(2019, 6, 10),
                LocalDate.of(2019, 6, 20),
                LocalDate.of(2019, 8, 15),
                LocalDate.of(2019, 10, 3),
                LocalDate.of(2019, 11, 1),
                LocalDate.of(2019, 12, 24),
                LocalDate.of(2019, 12, 25),
                LocalDate.of(2019, 12, 26),
                LocalDate.of(2019, 12, 31),
                // 2020
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 1, 6),
                LocalDate.of(2020, 4, 10),
                LocalDate.of(2020, 4, 12),
                LocalDate.of(2020, 5, 1),
                LocalDate.of(2020, 5, 21),
                LocalDate.of(2020, 5, 31),
                LocalDate.of(2020, 6, 1),
                LocalDate.of(2020, 6, 11),
                LocalDate.of(2020, 8, 15),
                LocalDate.of(2020, 10, 3),
                LocalDate.of(2020, 11, 1),
                LocalDate.of(2020, 12, 24),
                LocalDate.of(2020, 12, 25),
                LocalDate.of(2020, 12, 26),
                LocalDate.of(2020, 12, 31)
        );

        boolean isPublicHoliday(LocalDate date) {
            return publicHolidays.contains(date);
        }
    }

    private static class CalendarLine {
        private static final int FIRST_LINE_Y = 50;
        private static final int SECOND_LINE_Y = 75;
        private static final int STEP_WIDTH = 40;

        static void render(Graphics2D g, int offset, com.flopcode.slideshow.Font smallFont, LocalDate now, PublicHolidays publicHolidays) {
            LocalDate current = LocalDate.of(now.getYear(), now.getMonth(), 1);
            int i = 1;
            ColorScheme whiteOrBlack = new ColorScheme(Color.white);
            ColorScheme redOrBlack = new ColorScheme(Color.red);
            ColorScheme publicHoliday = new ColorScheme(new Color(0x71A95A));
            while (current.getMonthValue() == now.getMonthValue()) {
                ColorScheme cs = publicHolidays.isPublicHoliday(current) ? publicHoliday :
                        current.getDayOfWeek() == SUNDAY ? redOrBlack : whiteOrBlack;
                boolean renderingCurrentDay = current.equals(now);
                centerDay(g, current, offset, i++, smallFont, renderingCurrentDay, cs);
                current = current.plusDays(1);
            }
        }

        static private void centerDay(Graphics2D g, LocalDate date, int offset, int i, com.flopcode.slideshow.Font font, boolean currentDay, ColorScheme cs) {
            g.setFont(font.font);

            String dayOfMonth = "" + date.getDayOfMonth();
            String dayOfWeek = ("" + date.getDayOfWeek()).substring(0, 1);

            Rectangle2D bounds = font.metrics.getStringBounds(dayOfMonth, g);
            Rectangle2D dayOfWeekBounds = font.metrics.getStringBounds(dayOfWeek, g);

            int width = (int) Math.max(bounds.getWidth(), dayOfWeekBounds.getWidth());
            if (currentDay) {
                int border = 3;
                g.setColor(new Color(1, 1, 1, 0.9f));
                int upperBorder = font.metrics.getMaxAscent();
                g.drawRect(offset + i * STEP_WIDTH - width / 2 - border, FIRST_LINE_Y - upperBorder, width + 2 * border, 25 + upperBorder + font.metrics.getMaxDescent());
            }
            g.setColor(cs.normalColor);
            g.drawString(dayOfMonth, offset + i * STEP_WIDTH - ((int) bounds.getWidth() / 2), FIRST_LINE_Y);
            g.drawString(dayOfWeek, offset + i * STEP_WIDTH - ((int) dayOfWeekBounds.getWidth() / 2), SECOND_LINE_Y);
        }

    }

    private static class CalendarBackground {
        static void render(Graphics2D g, Dimension screenSize) {
            g.setComposite(AlphaComposite.getInstance(SRC_OVER, 1.0f));
            g.setColor(new Color(0, 0, 0, 0.7f));
            g.setStroke(new BasicStroke(1.5f));
            g.fillRect(0, 0, screenSize.width, 120);
        }
    }

    private static class CalendarDate {
        static void render(Graphics2D g, com.flopcode.slideshow.Font bigFont, LocalDate now, Locale locale) {
            g.setFont(bigFont.font);
            g.setColor(Color.WHITE);
            String dateFirstLine = now.getDayOfWeek().getDisplayName(SHORT_STANDALONE, locale) + " " + now.getDayOfMonth() + ".";
            String dateSecondLine = now.getMonth().getDisplayName(SHORT_STANDALONE, locale) + " " + now.getYear();
            g.drawString(dateFirstLine, 20, 50);
            g.drawString(dateSecondLine, 20, 100);
        }
    }
}
