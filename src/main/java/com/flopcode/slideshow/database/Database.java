package com.flopcode.slideshow.database;

import mindroid.os.Bundle;
import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Database extends HandlerThread {

    public Handler fileReceiver;
    public Handler imageRequest;
    private List<DatabaseImage> allImages = new ArrayList<>();
    private CountDownLatch usable = new CountDownLatch(1);
    private Handler requester = null;


    public Database() throws Exception {
        start();
        usable.await();
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        fileReceiver = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    DatabaseImage image = (DatabaseImage) msg.getData().getObject("image");
                    if (image == null) {
                        throw new IllegalArgumentException("expected image in bundle");
                    }
                    addImage(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                msg.recycle();
            }
        };
        imageRequest = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    requester = (Handler) msg.getData().getObject("requestor");
                    if (requester == null) {
                        throw new IllegalArgumentException("expected requestor in bundle");
                    }
                    sendBackToRequestor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                msg.recycle();
            }
        };

        usable.countDown();
    }

    private void addImage(DatabaseImage databaseImage) {
        allImages.add(databaseImage);
        sendBackToRequestor();
    }

    private void sendBackToRequestor() {
        if (requester != null) {
            DatabaseImage nextImage = next();
            if (nextImage != null) {
                requester.sendMessage(new Message().setData(new Bundle().putObject("image", nextImage)));
                requester = null;
            }
        }
    }

    public DatabaseImage next() {
        int index = (int) Math.floor(Math.random() * allImages.size());
        if (index >= allImages.size()) {
            return null;
        }

        return allImages.get(index);
    }
}
