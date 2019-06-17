package com.flopcode.slideshow;

import com.flopcode.slideshow.database.DatabaseImage;
import mindroid.os.Bundle;
import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

import java.awt.Dimension;
import java.util.concurrent.CountDownLatch;

public class Slideshow extends HandlerThread {

    private final Handler database;
    SlideshowCanvas canvas;
    private Handler imageAvailable;
    private Handler nextStep;
    private CountDownLatch ready = new CountDownLatch(1);
    Handler pause;
    Handler resume;
    private boolean paused = false;


    Slideshow(Handler db, Dimension screenSize) throws Exception {
        this.database = db;
        GeoLocationCache geoLocationCache = new GeoLocationCache();
        canvas = new SlideshowCanvas(screenSize, geoLocationCache);

        start();
    }

    void startup() throws Exception {
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
}
