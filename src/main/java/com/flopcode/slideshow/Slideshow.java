package com.flopcode.slideshow;

import com.flopcode.slideshow.database.DatabaseImage;
import com.flopcode.slideshow.weather.Weather.WeatherInfo;
import mindroid.os.Bundle;
import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

import java.awt.Dimension;
import java.time.Duration;

public class Slideshow extends HandlerThread {

    private final Handler database;
    private static final Duration NEXT_IMAGE = Duration.ofSeconds(2);
    SlideshowCanvas canvas;
    final Handler weather;
    final Handler pause;
    final Handler resume;
    private final Handler imageAvailable;
    private boolean paused = false;
    private final Handler nextStep;

    Slideshow(Handler db, Dimension screenSize) throws Exception {
        this.database = db;
        GeoLocationCache geoLocationCache = new GeoLocationCache();
        canvas = new SlideshowCanvas(screenSize, geoLocationCache);

        start();
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
        weather = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                WeatherInfo w = (WeatherInfo) msg.obj;
                canvas.setWeatherUi(w);
                msg.recycle();
            }
        };
    }

    void startup() {
        nextStep.sendMessage(new Message());
    }
}
