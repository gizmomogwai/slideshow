package com.flopcode.slideshow;

import com.flopcode.slideshow.database.DatabaseImage;
import com.google.common.base.Stopwatch;
import mindroid.os.Bundle;
import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;

import static java.awt.Color.BLACK;
import static java.awt.Image.SCALE_SMOOTH;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

public class Slideshow extends HandlerThread {

    private final Handler database;
    public SlideshowCanvas canvas;
    private Handler imageAvailable;
    private Handler nextStep;
    CountDownLatch ready = new CountDownLatch(1);
    public Handler pause;
    public Handler resume;
    private boolean paused = false;

    class SlideshowCanvas extends Canvas {
        private BufferStrategy buffers;
        private final Dimension screenSize;
        private Font font;
        private FontMetrics fontMetrics;
        DatabaseImage current;
        Image currentImage;

        SlideshowCanvas(Handler db, Dimension screenSize) throws Exception {
            setIgnoreRepaint(true);
            font = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("FFF Tusj.ttf")).deriveFont(64f);
            fontMetrics = getFontMetrics(font);
            this.screenSize = screenSize;
        }

        Graphics2D getGraphics2D(Dimension screenSize) {
            if (buffers == null) {
                createBufferStrategy(2);
                buffers = getBufferStrategy();
            }
            Graphics2D g = (Graphics2D) buffers.getDrawGraphics();
            g.setColor(BLACK);
            g.fillRect(0, 0, screenSize.width, screenSize.height);
            return g;
        }

        public void transitionTo(DatabaseImage next) throws Exception {
            if (next == null) {
                throw new IllegalArgumentException();
            }
            Image nextImage = loadImage(next.getFile(), screenSize);


            for (float i = 0; i < 1; i += 0.02) {
                Graphics2D g = getGraphics2D(screenSize);
                if (current != null) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1-i));
                    render(g, screenSize, current, currentImage);
                }
                if (next != null) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, i));
                    render(g, screenSize, next, nextImage);
                }
                g.dispose();
                buffers.show();
            }

            current = next;
            currentImage = nextImage;
            Graphics2D g = getGraphics2D(screenSize);
            render(g, screenSize, current, currentImage);
            g.dispose();
            buffers.show();
        }

        public void render(Graphics2D g, Dimension screenSize, DatabaseImage dbImage, Image image) {
            center(g, screenSize, image);
            title(g, screenSize, dbImage);
        }

        private void title(Graphics graphics, Dimension screenSize, DatabaseImage dbImage) {
            LocalDate now = LocalDate.now();
            String text = textForDate(now, dbImage);
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.setFont(font);
            g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);

            int x = fontMetrics.getMaxDescent() + 5;
            int y = screenSize.height - fontMetrics.getMaxDescent() - 5;
            g2d.setColor(Color.black);
            g2d.drawString(text, x - 1, y - 1);
            g2d.drawString(text, x + 1, y - 1);
            g2d.drawString(text, x + 1, y + 1);
            g2d.drawString(text, x - 1, y + 1);
            g2d.setColor(Color.white);
            g2d.drawString(text, x, y);
        }

        private String textForDate(LocalDate now, DatabaseImage databaseImage) {
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

        private void clear(Graphics graphics, Dimension screenSize) {
            graphics.setColor(BLACK);
            graphics.fillRect(0, 0, screenSize.width, screenSize.height);
        }

        private void center(Graphics graphics, Dimension screenSize, Image image) {
            graphics.drawImage(image, (screenSize.width - image.getWidth(null)) / 2, (screenSize.height - image.getHeight(null)) / 2, null);
        }

        Image loadImage(File file, Dimension screenSize) throws Exception {

            Stopwatch stopwatch = Stopwatch.createStarted();
            BufferedImage originalImage = ImageIO.read(file);
            System.out.println("time for reading image " + file + ": " + stopwatch.elapsed());
            stopwatch.reset().start();

            return fit(originalImage, screenSize);
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
    }

    public Slideshow(Handler db, Dimension screenSize) throws Exception {
        this.database = db;
        canvas = new SlideshowCanvas(db, screenSize);
        start();
    }

    public void startup() throws Exception {
        ready.await();
        nextStep.sendMessage(new Message());
    }

    @Override
    protected void onLooperPrepared() {
        imageAvailable = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    DatabaseImage newImage = (DatabaseImage) msg.getData().getObject("image");
                    System.out.println("Slideshow.handleMessage - got image");
                    if (!paused) {
                        canvas.transitionTo(newImage);
                        nextStep.sendMessageDelayed(new Message(), 4000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        nextStep = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    System.out.println("Slideshow.nextStep");
                    database.sendMessage(new Message().setData(new Bundle().putObject("requestor", imageAvailable)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        pause = new Handler(getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                try {
                    paused = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        resume = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    paused = false;
                    nextStep.sendMessage(new Message());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        ready.countDown();
    }
/*
    int getOffset() {
        return offset;
    }

    void setOffset(int offset) {
        loader.setOffset(this, offset);
        this.offset = offset;
    }

    public void run() throws Exception {
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
*/
}
