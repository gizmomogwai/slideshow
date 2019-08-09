package com.flopcode.slideshow.utils;

import com.flopcode.slideshow.database.DatabaseImage;
import mindroid.os.Bundle;
import mindroid.os.Handler;
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

public class FileScanner extends Thread {

    private final Handler fileReceiver;
    private final String[] baseDirs;

    public FileScanner(Handler fileReceiver, String... baseDirs) {
        this.fileReceiver = fileReceiver;
        this.baseDirs = baseDirs;
        start();
    }

    public void run() {
        for (String baseDir : baseDirs) {
            try {
                Path path = Paths.get(baseDir).normalize().toAbsolutePath();
                String pattern = "glob:" + path.toString() + "/**.{jpg,JPG,jpeg,JPEG}";
                final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (matcher.matches(file)) {
                            try {
                                DatabaseImage dbImage = DatabaseImage.create(file);
                                fileReceiver.sendMessage(new Message().setData(new Bundle().putObject("image", dbImage)));
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
                // long endTime = System.currentTimeMillis();
                // System.out.println("read database of " + allImages.size() + " in " + (endTime - startTime) / 1000 + "s");
                // finished();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
