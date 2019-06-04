package com.flopcode.slideshow;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.google.common.base.Stopwatch;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.awt.Image.SCALE_AREA_AVERAGING;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

public class Main {

    public static class SlideshowImage {
        final Image image;
        final DatabaseImage databaseImage;
        final int offset;
        private final Font font;
        private final FontMetrics fontMetrics;

        public SlideshowImage(Image image, DatabaseImage databaseImage, int offset, Font font, FontMetrics fontMetrics) {
            this.image = image;
            this.databaseImage = databaseImage;
            this.offset = offset;
            this.font = font;
            this.fontMetrics = fontMetrics;
        }

        public void draw(Graphics g, LocalDate now, int offset) {
            g.drawImage(image, this.offset - offset, 0, null);
            String text = textForDate(now);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setFont(font);
            g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);

            int x = this.offset - offset + fontMetrics.getMaxDescent() + 5;
            int y = image.getHeight(null) - fontMetrics.getMaxDescent() - 5;
            g2d.setColor(Color.black);
            g2d.drawString(text, x - 1, y - 1);
            g2d.drawString(text, x + 1, y - 1);
            g2d.drawString(text, x + 1, y + 1);
            g2d.drawString(text, x - 1, y + 1);
            g2d.setColor(Color.gray);
            g2d.drawString(text, x, y);
        }

        private String textForDate(LocalDate now) {
            if (databaseImage.creationData.getMonth() == now.getMonth()) {
                if (databaseImage.creationData.getDayOfMonth() == now.getDayOfMonth()) {
                    int yearDelta = now.getYear() - databaseImage.creationData.getYear();
                    if (yearDelta == 1) {
                        return "Last year";
                    } else {
                        return "" + yearDelta + " years ago";
                    }
                } else {
                    return String.format("%d-%02d", databaseImage.creationData.getYear(),
                            databaseImage.creationData.getMonth().getValue());
                }
            }
            return String.format("%d-%02d-%02d", databaseImage.creationData.getYear(),
                    databaseImage.creationData.getMonth().getValue(),
                    databaseImage.creationData.getDayOfMonth());
        }
    }

    public static class DatabaseImage {
        final Path path;
        final Dimension size;
        final LocalDate creationData;

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

    public static class GoodImages {
        private final java.util.List<DatabaseImage> allImages;
        private java.util.List<DatabaseImage> goodImages;
        LocalDate now;
        int index;

        GoodImages(java.util.List<DatabaseImage> allImages) {
            this.allImages = allImages;
            calcGoodImages(allImages);
        }

        private void calcGoodImages(List<DatabaseImage> allImages) {
            now = LocalDate.now();
            List<DatabaseImage> goodImages = allImages
                    .stream()
                    .filter(i -> sameMonth(now, i))
                    .collect(Collectors.toList());
            goodImages.sort(new DatabaseImageComparator(now));
            this.goodImages = goodImages;
            this.index = 0;
        }

        private boolean sameMonth(LocalDate d, DatabaseImage i) {
            return i.creationData.getMonth() == d.getMonth();
        }

        public DatabaseImage next(Database database, List<Database.Listener> listeners) {
            if (!now.isEqual(LocalDate.now())) {
                calcGoodImages(allImages);
            }
            DatabaseImage res = goodImages.get(index);
            index = (index + 1) % goodImages.size();
            for (Database.Listener l : listeners) {
                l.databaseChanged(database);
            }
            return res;
        }
    }

    public static class Database {
        private final int height;
        GoodImages goodImages;

        public int getTotalCount() {
            return goodImages.allImages.size();
        }

        public int getGoodImageCount() {
            return goodImages.goodImages.size();
        }

        public interface Listener {
            void databaseChanged(Database db);
        }

        public int getCurrentIndex() {
            return goodImages.index;
        }

        List<Listener> listeners = new ArrayList<>();

        Database(int height, String path) throws IOException {
            this.height = height;
            java.util.List<DatabaseImage> allImages = new ArrayList<>();

            long startTime = System.currentTimeMillis();
            String s = Paths.get(path).normalize().toAbsolutePath().toString();
            String pattern = "glob:" + s + "/**/*.{jpg,JPG,jpeg,JPEG}";
            final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
            Files.walkFileTree(Paths.get(path).toAbsolutePath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (matcher.matches(file)) {

                        try {
                            allImages.add(DatabaseImage.create(file));
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
            goodImages = new GoodImages(allImages);
        }

        public Database addListener(Listener l) {
            listeners.add(l);
            l.databaseChanged(this);
            return this;
        }

        SlideshowImage next(int offset, Font font, FontMetrics fontMetrics) throws Exception {
            DatabaseImage res = goodImages.next(this, listeners);
            Stopwatch stopwatch = Stopwatch.createStarted();
            BufferedImage read = ImageIO.read(res.getFile());
            System.out.println("time for reading image " + res.getFile() + ": " + stopwatch.elapsed());
            stopwatch.reset().start();
            Image scaledInstance = read.getScaledInstance(-1, height, SCALE_AREA_AVERAGING);
            System.out.println("time for scaling image " + res.getFile() + ": " + stopwatch.elapsed());
            return new SlideshowImage(scaledInstance, res, offset, font, fontMetrics);
        }
    }

    public static class Slideshow extends JComponent {

        private final Database database;
        private final int width;
        private final Font font;
        private final FontMetrics fontMetrics;
        private int totalX;
        private int offset;
        private ArrayList<SlideshowImage> images = new ArrayList<>();

        public Slideshow(Database db, int width) throws Exception {
            this.database = db;
            this.offset = 0;
            this.width = width;
            this.totalX = 0;
            this.font = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("FFF Tusj.ttf")).deriveFont(64f);
            this.fontMetrics = getFontMetrics(font);
            fill();
            setAlignmentX(0.0f);
            setAlignmentY(0.0f);
        }

        @Override
        protected void paintComponent(Graphics g) {
            LocalDate now = LocalDate.now();
            for (SlideshowImage image : images) {
                image.draw(g, now, offset);
            }
        }

        int getOffset() {
            return offset;
        }

        void setOffset(int offset) throws Exception {
            this.offset = offset;
            fill();
            repaint();
        }

        private void fill() throws Exception {
            {
                Iterator<SlideshowImage> i = images.iterator();
                while (i.hasNext()) {
                    SlideshowImage image = i.next();

                    if (image.offset + image.image.getWidth(null) < offset) {
                        i.remove();
                    }
                }
            }
            while (true) {
                if (images.size() > 0) {
                    SlideshowImage last = images.get(images.size() - 1);
                    if (last.offset + last.image.getWidth(null) > offset + width) {
                        break;
                    }
                }
                SlideshowImage i = database.next(totalX, font, fontMetrics);
                totalX += i.image.getWidth(null);
                images.add(i);
            }
        }
    }

    public static class Statistics extends JLabel {

        public Statistics(Database database) throws Exception {
            setForeground(Color.red);
            setAlignmentX(0.0f);
            setAlignmentY(0.0f);
            database.addListener(db -> updateText(db));
        }

        private void updateText(Database database) {
            setText("total: " + database.getTotalCount() + "  selected: " + database.getGoodImageCount() + "  current: " + database.getCurrentIndex());
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("screenSize = " + screenSize);

        Database db = new Database(screenSize.height, args[0]);

        Slideshow imageList = new Slideshow(db, screenSize.width);

        Statistics statistics = new Statistics(db);
        statistics.setVisible(false);
        JFrame root = new JFrame("Slideshow");
        root.setUndecorated(true);
        root.setSize(screenSize);
        JPanel all = new JPanel();
        all.setLayout(new OverlayLayout(all));
        all.add(statistics);
        all.add(imageList);
        root.setContentPane(all);
        root.setVisible(true);
        new Timer(1000 / 30, e -> {
            try {
                imageList.setOffset(imageList.getOffset() + 1);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).start();
    }
}
