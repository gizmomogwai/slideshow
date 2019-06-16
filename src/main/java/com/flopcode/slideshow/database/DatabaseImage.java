package com.flopcode.slideshow.database;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;

import java.awt.Dimension;
import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.channels.Channels.newInputStream;

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
        Metadata metadata = readMetadata(path);
        // printMetadata(path, metadata);
        Dimension size = getSize(path, metadata);
        LocalDate creationDate = getCreationDate(path, metadata);
        return new DatabaseImage(path, size, creationDate);
    }

    private static Metadata readMetadata(Path path) throws Exception {
        System.out.println("path = " + path);
        try (FileChannel channel = FileChannel.open(path)) {
            return ImageMetadataReader.readMetadata(newInputStream(channel));
        }
    }

    private static LocalDate getCreationDate(Path path, Metadata metadata) throws Exception {
        try {
            ExifSubIFDDirectory dateDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (dateDirectory == null) {
                throw new Exception("Cannot find ExifSubIFD for " + path);
            }
            Date date = dateDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            Calendar c = new GregorianCalendar();
            c.setTime(date);
            return LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
            Matcher matcher = Pattern.compile(".*(\\d{4})/(\\d{2})/(\\d{2}).*").matcher(path.toString());
            if (matcher.matches()) {
                LocalDate res = LocalDate.of(Integer.parseInt(matcher.group(1)),
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3)));
                System.out.println("Falling back to date from filename " + path + "=" + res);
                return res;
            }
            throw new RuntimeException("Cannot get date for " + path, e);
        }
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
