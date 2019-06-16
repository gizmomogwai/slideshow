package com.flopcode.slideshow;

import com.flopcode.slideshow.database.DatabaseImage;
import mindroid.os.Bundle;
import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CountDownLatch;

import static java.awt.Color.BLACK;
import static java.awt.Image.SCALE_SMOOTH;

public class Slideshow extends HandlerThread {

    private final Handler database;
    private final GeoLocationCache geoLocationCache;
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

        SlideshowImage current;

        SlideshowCanvas(Handler db, Dimension screenSize) throws Exception {
            setIgnoreRepaint(true);
            font = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("FFF Tusj.ttf")).deriveFont(48f);
            fontMetrics = getFontMetrics(font);
            this.screenSize = screenSize;
            current = new SlideshowImage(DatabaseImage.dummy(), new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY), font, fontMetrics, geoLocationCache);
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
            SlideshowImage nextImage = new SlideshowImage(next, loadImage(next.getFile(), screenSize), font, fontMetrics, geoLocationCache);

            for (float i = 0; i < 1; i += 0.02) {
                Graphics2D g = getGraphics2D(screenSize);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1 - i));
                current.render(g, screenSize);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, i));
                nextImage.render(g, screenSize);
                g.dispose();
                buffers.show();
            }

            current.dispose();

            current = nextImage;
            Graphics2D g = getGraphics2D(screenSize);
            current.render(g, screenSize);
            g.dispose();
            buffers.show();
        }

        Image loadImage(File file, Dimension screenSize) throws Exception {
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
    }

    public Slideshow(Handler db, Dimension screenSize) throws Exception {
        this.database = db;
        canvas = new SlideshowCanvas(db, screenSize);
        geoLocationCache = new GeoLocationCache();

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
                DatabaseImage newImage = (DatabaseImage) msg.getData().getObject("image");
                try {
                    if (newImage == null) {
                        throw new IllegalArgumentException();
                    }
                    System.out.println("Slideshow.handleMessage - got image " + newImage);
                    if (!paused) {
                        canvas.transitionTo(newImage);
                    }
                } catch (Throwable e) {
                    System.out.println("Slideshow.handleMessage - cannot handle " + newImage);
                    e.printStackTrace();
                }
                nextStep.sendMessageDelayed(new Message(), 2000);
                msg.recycle();
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
                msg.recycle();
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
                msg.recycle();
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
                msg.recycle();
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
