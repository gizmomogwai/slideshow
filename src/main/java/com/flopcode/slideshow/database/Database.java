package com.flopcode.slideshow.database;

import mindroid.os.Bundle;
import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Database extends HandlerThread {

    public Handler fileReceiver;
    List<DatabaseImage> allImages = new ArrayList<>();
    GoodImages goodImages = new GoodImages(new ArrayList<>());

    List<Listener> listeners = new ArrayList<>();
    CountDownLatch usable = new CountDownLatch(1);
    public Handler imageRequest;
    private Handler requestor = null;


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
            }
        };
        imageRequest = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    System.out.println("Database.handleMessage - imagerequest");
                    requestor = (Handler) msg.getData().getObject("requestor");
                    if (requestor == null) {
                        throw new IllegalArgumentException("expected requestor in bundle");
                    }
                    sendBackToRequestor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        usable.countDown();
    }

    private Database scan(String path) {
        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();
                String s = Paths.get(path).normalize().toAbsolutePath().toString();
                String pattern = "glob:" + s + "/**.{jpg,JPG,jpeg,JPEG}";
                final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
                Files.walkFileTree(Paths.get(path).toAbsolutePath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (matcher.matches(file)) {
                            try {
                                addImage(DatabaseImage.create(file));
                            } catch (Exception e) {
                                System.out.println("Problems with " + file + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }
                });
                long endTime = System.currentTimeMillis();
                System.out.println("read database of " + allImages.size() + " in " + (endTime - startTime) / 1000 + "s");
                finished();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        return this;
    }

    private void addImage(DatabaseImage databaseImage) {
        allImages.add(databaseImage);
        sendBackToRequestor();
/*


        if (System.currentTimeMillis() - lastUpdate > 1000) {
            goodImages = new GoodImages(allImages);
            if (goodImages.goodImages.size() > 0) {
                System.out.println("Database.addImage - los gehts");
                usable.countDown();
            }
            notifyListeners();
            lastUpdate = System.currentTimeMillis();
        }
        */

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

    private void finished() {
        goodImages = new GoodImages(allImages);
        notifyListeners();
    }

    public void notifyListeners() {
        for (Listener l : listeners) {
            l.databaseChanged(this);
        }
    }


    public Database addListener(Listener l) {
        listeners.add(l);
        l.databaseChanged(this);
        return this;
    }

    public int getTotalCount() {
        return goodImages.allImages.size();
    }

    public int getGoodImageCount() {
        return goodImages.goodImages.size();
    }

    public DatabaseImage next() {
        int index = (int) Math.floor(Math.random() * allImages.size());
        if (index >= allImages.size()) {
            return null;
        }

        return allImages.get(index);
    }

    public interface Listener {
        void databaseChanged(Database db);
    }
}
