package com.flopcode.slideshow.processes;

import com.flopcode.slideshow.Whiteboard;
import com.flopcode.slideshow.data.images.DatabaseImage;
import mindroid.os.Bundle;
import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Database extends HandlerThread {

    private final Whiteboard whiteboard;
    public Handler fileReceiver;
    public Handler imageRequest;
    private Handler requestor = null;

    private List<DatabaseImage> allImages = new ArrayList<>();
    private FilteredList filteredImages = new FilteredList();

    public Database(Whiteboard whiteboard) {
        this.whiteboard = whiteboard;
        start();
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
            }
        };
        imageRequest = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    requestor = (Handler) msg.getData().getObject("requestor");
                    if (requestor == null) {
                        throw new IllegalArgumentException("expected requestor in bundle");
                    }
                    filteredImages.update(allImages);
                    sendBackToRequestor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void addImage(DatabaseImage databaseImage) {
        allImages.add(databaseImage);
        if (filteredImages.add(databaseImage)) {
            sendBackToRequestor();
        }
        whiteboard.set("databaseStatistics", new Statistics(allImages.size(), filteredImages.images.size()));
    }

    private void sendBackToRequestor() {
        if (requestor != null) {
            DatabaseImage nextImage = next();
            if (nextImage != null) {
                requestor.sendMessage(new Message().setData(new Bundle().putObject("image", nextImage)));
                requestor = null;
            }
        }
    }

    public DatabaseImage next() {
        return filteredImages.next();
    }

    public static class Statistics {
        public final int totalImages;
        public final int filteredImages;

        Statistics(int totalImages, int filteredImages) {
            this.totalImages = totalImages;
            this.filteredImages = filteredImages;
        }
    }

    static class FilteredList {
        private final LocalDate now;
        private List<DatabaseImage> images = new ArrayList<>();
        private Predicate<DatabaseImage> filter;

        FilteredList() {
            now = LocalDate.now();
            updatePredicate();
        }

        private void updatePredicate() {
            filter = (image) -> image.creationData.getMonth() == LocalDate.now().getMonth();
        }

        public boolean add(DatabaseImage image) {
            if (filter.test(image)) {
                images.add(image);
                return true;
            }
            return false;
        }

        public void update(List<DatabaseImage> allImages) {
            if (!LocalDate.now().isEqual(now)) {
                updatePredicate();
                images = new ArrayList<>();
                for (DatabaseImage i : allImages) {
                    add(i);
                }
            }
        }

        public DatabaseImage next() {
            int index = (int) Math.floor(Math.random() * images.size());
            System.out.println("FilteredList.next index=" + index + " images.size=" + images.size());
            if (index >= images.size()) {
                return null;
            }
            return images.get(index);
        }
    }
}
