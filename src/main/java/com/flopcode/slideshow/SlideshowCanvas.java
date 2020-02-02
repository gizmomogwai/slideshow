package com.flopcode.slideshow;

import com.flopcode.slideshow.clock.Clock;
import com.flopcode.slideshow.data.images.DatabaseImage;
import com.flopcode.slideshow.data.moon.Moon;
import com.flopcode.slideshow.logger.Logger;
import com.flopcode.slideshow.ui.CalendarUI;
import com.flopcode.slideshow.ui.Gfx;
import com.flopcode.slideshow.ui.MoonUI;
import com.flopcode.slideshow.ui.OnTopUI;
import com.flopcode.slideshow.ui.StatisticsUI;
import com.flopcode.slideshow.ui.WeatherUI;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.BiConsumer;

import static java.awt.AlphaComposite.SRC_OVER;
import static java.awt.Color.BLACK;
import static java.awt.Font.TRUETYPE_FONT;
import static java.awt.Font.createFont;
import static java.awt.Image.SCALE_SMOOTH;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

public class SlideshowCanvas extends Canvas {
    private final GeoLocationCache geoLocationCache;
    private final Dimension screenSize;
    private final OnTopUI onTop;
    private Fonts fonts;
    private BufferStrategy buffers;
    private SlideshowImage current;

    SlideshowCanvas(Logger logger, Clock clock, WhiteboardForHandler whiteboardForHandler, Dimension screenSize, GeoLocationCache geoLocationCache) throws Exception {
        this.geoLocationCache = geoLocationCache;
        Moon moon = new Moon();
        fonts = new Fonts(this, createFont(TRUETYPE_FONT, Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("FFF Tusj.ttf"))));
        PublicHolidays publicHolidays = new PublicHolidays();

        setIgnoreRepaint(true);
        this.screenSize = screenSize;
        current = new SlideshowImage(logger, clock, DatabaseImage.dummy(), new BufferedImage(1, 1, TYPE_BYTE_GRAY), fonts.subtitles, geoLocationCache);
        onTop = new OnTopUI(fonts,
                new CalendarUI(clock, screenSize, fonts, publicHolidays),
                new MoonUI(moon),
                new WeatherUI(logger, clock, whiteboardForHandler, fonts),
                new StatisticsUI(whiteboardForHandler, screenSize, fonts));
    }

    private <T> void renderDoubleBuffered(Dimension screenSize, T context, BiConsumer<Gfx, T> renderer) {
        if (buffers == null) {
            createBufferStrategy(2);
            buffers = getBufferStrategy();
        }
        Graphics2D g = (Graphics2D) buffers.getDrawGraphics();
        Gfx gfx = new Gfx(g, 0, 0, screenSize.width, screenSize.height);

        gfx.setColor(BLACK);
        gfx.fillRect(0, 0, screenSize.width, screenSize.height);
        renderer.accept(gfx, context);
        gfx.dispose();
        buffers.show();
    }

    void transitionTo(Logger logger, Clock clock, DatabaseImage next) throws Exception {
        if (next == null) {
            throw new IllegalArgumentException();
        }
        SlideshowImage nextImage = new SlideshowImage(logger, clock, next, loadImage(next.getFile(), screenSize), fonts.subtitles, geoLocationCache);

        for (float i = 0; i < 1; i += 0.02) {

            renderDoubleBuffered(screenSize, i, (g, alpha) -> {
                g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                g.setComposite(AlphaComposite.getInstance(SRC_OVER, 1 - alpha));
                current.render(g, screenSize);
                g.setComposite(AlphaComposite.getInstance(SRC_OVER, alpha));
                nextImage.render(g, screenSize);

                g.render(onTop, 0, 0);
            });
        }

        current.dispose();
        current = nextImage;

        renderDoubleBuffered(screenSize, null, (g, nothing) -> {
            g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            current.render(g, screenSize);

            g.render(onTop, 0, 0);
        });
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

    public static class Fonts {
        public final com.flopcode.slideshow.ui.Font calendar;
        public final com.flopcode.slideshow.ui.Font subtitles;
        public final com.flopcode.slideshow.ui.Font weather;
        public final com.flopcode.slideshow.ui.Font dateTime;

        Fonts(Component c, Font baseFont) {
            this.subtitles = new com.flopcode.slideshow.ui.Font(c, baseFont.deriveFont(48f));
            this.dateTime = this.subtitles;
            this.weather = new com.flopcode.slideshow.ui.Font(c, baseFont.deriveFont(32f));
            this.calendar = new com.flopcode.slideshow.ui.Font(c, baseFont.deriveFont(28f));
        }
    }

    private static class ColorScheme {
        final Color normal;
        final Color sunday;
        final Color publicHoliday;

        private ColorScheme(Color normal, Color sunday, Color publicHoliday) {
            this.normal = normal;
            this.sunday = sunday;
            this.publicHoliday = publicHoliday;
        }
    }

    public static class PublicHolidays {
        HashSet<LocalDate> publicHolidays = new HashSet<>();

        {
            publicHolidays.addAll(Arrays.asList(
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
            ));
        }

        boolean isPublicHoliday(LocalDate date) {
            return publicHolidays.contains(date);
        }
    }

    public static class CalendarLine {
        private static final int FIRST_LINE_Y = 53;
        private static final int SECOND_LINE_Y = 82;
        private static final int STEP_WIDTH = 48;

        public static void render(Graphics2D g, int offset, com.flopcode.slideshow.ui.Font smallFont, LocalDate now, PublicHolidays publicHolidays) {
            LocalDate current = now;
            int nrOfDays = 31;
            ColorScheme colorScheme = new ColorScheme(Color.white, Color.red, new Color(0x71A95A));
            int i = 1;
            while (nrOfDays > 0) {
                nrOfDays--;
                Color color = publicHolidays.isPublicHoliday(current) ? colorScheme.publicHoliday :
                        (current.getDayOfWeek() == SUNDAY || current.getDayOfWeek() == SATURDAY) ? colorScheme.sunday : colorScheme.normal;
                boolean renderingCurrentDay = current.equals(now);
                centerDay(g, current, offset, i++, smallFont, renderingCurrentDay, color);
                LocalDate next = current.plusDays(1);
                if (current.getMonth() != next.getMonth()) {
                    g.setColor(Color.WHITE);
                    int x = (int) (offset + (i-0.5)*STEP_WIDTH)+4;
                    g.drawLine(x, FIRST_LINE_Y-smallFont.metrics.getAscent()+5, x, SECOND_LINE_Y+5);
                }
                current = next;
            }
        }

        static private void centerDay(Graphics2D g, LocalDate date, int offset, int i, com.flopcode.slideshow.ui.Font font, boolean currentDay, Color color) {
            g.setFont(font.font);

            String dayOfMonth = "" + date.getDayOfMonth();
            String dayOfWeek = ("" + date.getDayOfWeek()).substring(0, 1);

            Rectangle2D bounds = font.metrics.getStringBounds(dayOfMonth, g);
            Rectangle2D dayOfWeekBounds = font.metrics.getStringBounds(dayOfWeek, g);

            int width = (int) Math.max(bounds.getWidth(), dayOfWeekBounds.getWidth());
            if (currentDay) {
                int border = 3;
                g.setColor(new Color(1, 1, 1, 0.2f));
                int upperBorder = font.metrics.getMaxAscent();
                g.fillRect(offset + i * STEP_WIDTH - width / 2 - border, FIRST_LINE_Y - upperBorder, width + 2 * border, 25 + upperBorder + font.metrics.getMaxDescent());
            }
            g.setColor(color);
            g.drawString(dayOfMonth, offset + i * STEP_WIDTH - ((int) bounds.getWidth() / 2), FIRST_LINE_Y);
            g.drawString(dayOfWeek, offset + i * STEP_WIDTH - ((int) dayOfWeekBounds.getWidth() / 2), SECOND_LINE_Y);
        }
    }
}
