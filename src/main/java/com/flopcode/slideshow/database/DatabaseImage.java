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

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;

import java.awt.Dimension;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatabaseImage {
    public final LocalDate creationData;
    final Path path;
    final Dimension size;

    public DatabaseImage(Path path, Dimension size, LocalDate creationData) {
        this.path = path;
        this.size = size;
        this.creationData = creationData;
    }

    public static DatabaseImage create(Path path) throws Exception {
        Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
        // printMetadata(path, metadata);
        Dimension size = getSize(path, metadata);
        LocalDate creationDate = getCreationDate(path, metadata);
        return new DatabaseImage(path, size, creationDate);
    }

    private static LocalDate getCreationDate(Path path, Metadata metadata) throws Exception {
        ExifSubIFDDirectory dateDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (dateDirectory == null) {
            throw new Exception("Cannot find ExifSubIFD for " + path);
        }
        Date date = dateDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        return LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    private static Dimension getSize(Path path, Metadata metadata) throws Exception {
        Directory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
        if (jpegDirectory == null) {
            throw new Exception("Cannot find JpegDirectory for " + path);
        }
        int width = jpegDirectory.getInt(JpegDirectory.TAG_IMAGE_WIDTH);
        int height = jpegDirectory.getInt(JpegDirectory.TAG_IMAGE_HEIGHT);
        return new Dimension(width, height);
    }

    private static void printMetadata(Path path, Metadata metadata) {
        System.out.println("path = " + path);
        for (Directory directory : metadata.getDirectories()) {
            System.out.println("  directory = " + directory);
            for (Tag tag : directory.getTags()) {
                System.out.format("    [%s] - %s = %s\n",
                        directory.getName(), tag.getTagName(), tag.getDescription());
            }
            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    System.err.format("    ERROR: %s\n", error);
                }
            }
        }
    }

    public File getFile() {
        return path.toFile();
    }
}
