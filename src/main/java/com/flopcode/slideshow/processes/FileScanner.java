package com.flopcode.slideshow.processes;

import com.flopcode.slideshow.data.images.DatabaseImage;
import com.flopcode.slideshow.logger.Logger;
import com.google.common.base.Stopwatch;
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
    private final Handler doneReceiver;
    private final String[] baseDirs;
    private final Logger logger;

    public FileScanner(Logger logger, Handler fileReceiver, Handler doneReceiver, String... baseDirs) {
        this.logger = logger;
        this.fileReceiver = fileReceiver;
        this.doneReceiver = doneReceiver;
        this.baseDirs = baseDirs;
        start();
    }

    public void run() {
        Stopwatch scanning = Stopwatch.createStarted();
        for (String baseDir : baseDirs) {
            try {
                Path path = Paths.get(baseDir).normalize().toAbsolutePath();
                String pattern = "glob:" + path + "/**.{jpg,JPG,jpeg,JPEG}";
                final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (matcher.matches(file)) {
                            try {
                                DatabaseImage dbImage = DatabaseImage.create(logger, file);
                                fileReceiver.sendMessage(new Message().setData(new Bundle().putObject("image", dbImage)));
                            } catch (Exception e) {
                                logger.e("Problems with " + file + ": " + e.getMessage());
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
                doneReceiver.sendMessage(new Message());
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.i("All scanned in " + scanning.elapsed().getSeconds());
        }
    }
}
