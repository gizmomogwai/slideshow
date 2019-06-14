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
package com.flopcode.slideshow.database;

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

public class Database {

    List<DatabaseImage> allImages = new ArrayList<>();
    GoodImages goodImages = new GoodImages(new ArrayList<>());

    List<Listener> listeners = new ArrayList<>();
    CountDownLatch usable = new CountDownLatch(1);

    private Database() {
    }

    public static Database fromPath(String path) {
        return new Database().scan(path);
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

    long lastUpdate;
    private synchronized void addImage(DatabaseImage databaseImage) {
        allImages.add(databaseImage);
        if (System.currentTimeMillis() - lastUpdate > 1000) {
            goodImages = new GoodImages(allImages);
            if (goodImages.goodImages.size() > 0) {
                System.out.println("Database.addImage - los gehts");
                usable.countDown();
            }
            notifyListeners();
            lastUpdate = System.currentTimeMillis();
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

    public synchronized DatabaseImage next() throws Exception {
        usable.await();
        DatabaseImage next = goodImages.next(this);
        System.out.println("Database.next -> " + next);
        return next;
    }

    public interface Listener {
        void databaseChanged(Database db);
    }
}
