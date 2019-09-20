package com.flopcode.slideshow;

import com.flopcode.slideshow.data.images.DatabaseImage;
import mindroid.os.Bundle;
import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

import java.awt.Dimension;
import java.time.Duration;

class Slideshow extends HandlerThread {

    private static final Duration NEXT_IMAGE = Duration.ofMinutes(1);
    final Handler pause;
    final Handler resume;
    private final Handler database;
    private final Handler imageAvailable;
    private final Handler nextStep;
    SlideshowCanvas canvas;
    private boolean paused = false;

    Slideshow(Handler db, Whiteboard whiteboard, Dimension screenSize) throws Exception {
        this.database = db;
        GeoLocationCache geoLocationCache = new GeoLocationCache();

        start();
        Handler handler = new Handler(getLooper());
        canvas = new SlideshowCanvas(new WhiteboardForHandler(whiteboard, handler), screenSize, geoLocationCache);
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
                nextStep.sendMessageDelayed(new Message(), NEXT_IMAGE.toMillis());
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
                    // nextStep.sendMessage(new Message());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
    }

    void startup() {
        nextStep.sendMessage(new Message());
    }
}
