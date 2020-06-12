package com.flopcode.slideshow.processes;

import com.flopcode.slideshow.Whiteboard;
import com.flopcode.slideshow.clock.Clock;
import com.flopcode.slideshow.data.images.DatabaseImage;
import com.flopcode.slideshow.logger.Logger;
import mindroid.os.Bundle;
import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class Database extends HandlerThread {

    public Handler fileReceiver;
    public Handler imageRequest;
    private Handler requestor = null;

    private List<DatabaseImage> allImages = new ArrayList<>();
    private FilteredList filteredImages;

    public Database(Logger logger, Clock clock, Whiteboard whiteboard) {
        this.filteredImages = new FilteredList(logger, clock, whiteboard);
        start();
        fileReceiver = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    DatabaseImage image = (DatabaseImage) msg.getData().getObject("image");
                    if (image == null) {
                        throw new IllegalArgumentException("expected image in bundle");
                    }
                    addImage(allImages, image);
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

    private void addImage(List<DatabaseImage> allImages, DatabaseImage databaseImage) {
        allImages.add(databaseImage);
        if (filteredImages.add(allImages, databaseImage)) {
            sendBackToRequestor();
        }
    }

    private void sendBackToRequestor() {
        if (requestor != null) {
            DatabaseImage nextImage = next();
            System.out.println("Database.sendBackToRequestor() " + nextImage);
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
        public final int currentImage;

        Statistics(int totalImages, int filteredImages, int currentImage) {
            this.totalImages = totalImages;
            this.filteredImages = filteredImages;
            this.currentImage = currentImage;
        }
    }

    static class FilteredList {
        private final Logger logger;
        private final Clock clock;
        private final Whiteboard whiteboard;
        private LocalDate now;
        private List<DatabaseImage> allImages = new ArrayList<>();
        private List<DatabaseImage> images = new ArrayList<>();
        private Predicate<DatabaseImage> filter;
        private int index = -1;

        FilteredList(Logger logger, Clock clock, Whiteboard whiteboard) {
            this.logger = logger;
            this.clock = clock;
            this.whiteboard = whiteboard;
            now = clock.date();
            updatePredicate();
        }

        private void updatePredicate() {
            filter = (image) -> true; // image.creationData.getMonth() == clock.date().getMonth();
        }

        private boolean add(List<DatabaseImage> allImages, DatabaseImage image) {
            this.allImages = allImages;
            if (filter.test(image)) {
                images.add(image);
                Collections.shuffle(images);
                index = -1;
                updateStatistics();
                return true;
            }
            updateStatistics();
            return false;
        }

        private void updateStatistics() {
            Statistics value = new Statistics(allImages.size(), images.size(), index);
            logger.d("Stats: " + value.currentImage + " / " + value.filteredImages + " / " + value.totalImages);
            whiteboard.set("databaseStatistics", value);
        }

        public void update(List<DatabaseImage> allImages) {
            if (!clock.date().isEqual(now)) {
                logger.i("Rescan images for " + clock.date());
                updatePredicate();
                images = new ArrayList<>();
                allImages.forEach(image -> add(allImages, image));
                now = clock.date();
            }
        }

        public DatabaseImage next() {
            logger.i("FilteredList.next index=" + index + " images.size=" + images.size() + " allImages.size=" + allImages.size());

            if (images.size() == 0) {
                return null;
            }
            index++;
            if (index == images.size()) {
                index = 0;
                Collections.shuffle(images);
            }
            updateStatistics();

            return images.get(index);
        }
    }
}
