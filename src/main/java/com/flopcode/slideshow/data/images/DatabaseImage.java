package com.flopcode.slideshow.data.images;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.flopcode.slideshow.logger.Logger;
import com.flopcode.slideshow.processes.FileScanner;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.channels.Channels.newInputStream;

public class DatabaseImage {
    public final LocalDate creationData;
    public final GeoLocation geoLocation;
    private final Path path;

    private DatabaseImage(Path path, LocalDate creationData, GeoLocation geoLocation) {
        this.path = path;
        this.creationData = creationData;
        this.geoLocation = geoLocation;
    }

    public static DatabaseImage create(Logger logger, Path path, FileScanner.ImageCache cache) throws Exception {
        if (cache.contains(path)) {
            DateAndLocation dateAndLocation = cache.get(path);
            return new DatabaseImage(path,
                    dateAndLocation.date,
                    dateAndLocation.location != null ?
                            new GeoLocation(dateAndLocation.location.latitude, dateAndLocation.location.longitute) : null);
        } else {
            Metadata metadata = readMetadata(path);
            // printMetadata(path, metadata);
            GeoLocation gps = getGps(metadata);
            LocalDate creationDate = getCreationDate(logger, path, metadata);

            cache.add(path, new DateAndLocation(creationDate, gps));
            return new DatabaseImage(path, creationDate, gps);
        }
    }

    private static GeoLocation getGps(Metadata metadata) {
        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDirectory != null) {
            return gpsDirectory.getGeoLocation();
        }
        return null;
    }

    private static Metadata readMetadata(Path path) throws Exception {
        try (FileChannel channel = FileChannel.open(path)) {
            return ImageMetadataReader.readMetadata(newInputStream(channel));
        }
    }

    private static LocalDate getCreationDate(Logger logger, Path path, Metadata metadata) {
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
                logger.i("Falling back to date from filename " + path + "=" + res);
                return res;
            }
            throw new RuntimeException("Cannot get date for " + path, e);
        }
    }

    public static DatabaseImage dummy() {
        return new DatabaseImage(Paths.get("."), LocalDate.now(), null);
    }

    public File getFile() {
        return path.toFile();
    }

    @Override
    public String toString() {
        return "DatabaseImage(file=" + getFile() + ")";
    }
}
