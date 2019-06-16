/*
 * Copyright (c) 2019 E.S.R.Labs. All rights reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of E.S.R.Labs and its suppliers, if any.
 * The intellectual and technical concepts contained herein are
 * proprietary to E.S.R.Labs and its suppliers and may be covered
 * by German and Foreign Patents, patents in process, and are protected
 * by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from E.S.R.Labs.
 */
package com.flopcode.slideshow;

import com.flopcode.slideshow.database.Database;
import com.flopcode.slideshow.database.DatabaseImage;
import com.google.common.base.Stopwatch;

import javax.imageio.ImageIO;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.awt.Image.SCALE_SMOOTH;


public class Slideshow extends Canvas {

    private final Database database;
    private final Dimension screenSize;
    private final Font font;
    private final FontMetrics fontMetrics;
    private final Loader loader;
    private int totalX;
    private int offset;
    private ArrayList<SlideshowImage> images = new ArrayList<>();

    public Slideshow(Database db, Dimension screenSize) throws Exception {
        this.database = db;
        this.screenSize = screenSize;
        this.offset = 0;
        this.totalX = 0;
        setIgnoreRepaint(true);
        this.font = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("FFF Tusj.ttf")).deriveFont(64f);
        this.fontMetrics = getFontMetrics(font);
        this.loader = new Loader();
    }

    public static Slideshow fromDatabase(Database db, Dimension screenSize) throws Exception {
        return new Slideshow(db, screenSize);
    }

    protected void paintComponent(Graphics g) {
        LocalDate now = LocalDate.now();
        synchronized (images) {
            for (SlideshowImage image : images) {
                image.draw(g, now, offset);
            }
        }
    }

    int getOffset() {
        return offset;
    }

    void setOffset(int offset) {
        loader.setOffset(this, offset);
        this.offset = offset;
    }

    public void run() throws Exception {
        createBufferStrategy(2);
        BufferStrategy buffers = getBufferStrategy();
        while (true) {
            setOffset(getOffset() + 1);
            Graphics graphics = buffers.getDrawGraphics();
            paintComponent(graphics);
            graphics.dispose();
            buffers.show();
        }
    }

    private static class Loader {
        private final ExecutorService pool;

        public Loader() {
            pool = Executors.newFixedThreadPool(1, r -> {
                Thread t = new Thread(r);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            });
        }

        public void setOffset(Slideshow slideshow, int offset) {
            pool.submit(() -> {
                try {
                    expireImages(slideshow, offset);
                    loadNewImages(slideshow, offset);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        private void loadNewImages(Slideshow slideshow, int offset) {
            try {
                while (true) {
                    if (slideshow.images.size() > 0) {
                        SlideshowImage last = slideshow.images.get(slideshow.images.size() - 1);
                        if (last.offset > offset + slideshow.screenSize.width) {
                            break;
                        }
                    }
                    try {
                        DatabaseImage i = slideshow.database.next();
                        if (i == null) {
                            break;
                        }
                        Stopwatch stopwatch = Stopwatch.createStarted();
                        BufferedImage originalImage = ImageIO.read(i.getFile());
                        System.out.println("time for reading image " + i.getFile() + ": " + stopwatch.elapsed());
                        stopwatch.reset().start();
                        Image scaledImage = originalImage.getScaledInstance(-1, slideshow.screenSize.height, SCALE_SMOOTH);
                        System.out.println("time for scaling image " + i.getFile() + ": " + stopwatch.elapsed());
                        SlideshowImage image = new SlideshowImage(scaledImage, i, slideshow.totalX, slideshow.font, slideshow.fontMetrics);
                        slideshow.totalX += image.image.getWidth(null);
                        synchronized (slideshow.images) {
                            slideshow.images.add(image);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void expireImages(Slideshow slideshow, int offset) {
            synchronized (slideshow.images) {
                Iterator<SlideshowImage> i = slideshow.images.iterator();
                while (i.hasNext()) {
                    SlideshowImage image = i.next();

                    if (image.offset + image.image.getWidth(null) < offset) {
                        i.remove();
                    }
                }
            }
        }
    }

}
