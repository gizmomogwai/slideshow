package com.flopcode.slideshow;

import com.flopcode.slideshow.database.Database;
import com.flopcode.slideshow.utils.FileScanner;
import com.flopcode.slideshow.weather.Weather;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Filescanner scans the filesystem and sends imagepaths to the database
 * Database gets an filter and provides images on request (filtered) this is an asyn operation.
 * maximum 1 request can be open
 * ImageLoader loads and scales images. it gets an load requests and completes it
 * Slideshow renders the animation as soon as the first image comes in
 * e.g. by incrementing an offset and sending this to
 * <p>
 * SlideshowLoader acts on the current offset, removes past images, requests new from the database, loads them and provides them then to the slideshow
 * <p>
 * startup
 * slideshow renders at the beginning, but does not advance
 * filescanner picks up a file sends it to database
 * database filters new images if it sees a request it completes this request
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("screenSize = " + screenSize);
        // screenSize.width /= 2;
        // screenSize.height /= 2;

        Database db = new Database();
        FileScanner scanner = new FileScanner(db.fileReceiver, args);
        Slideshow slideshow = new Slideshow(db.imageRequest, screenSize);
        MotionDetector motionDetector = new MotionDetector(slideshow.pause, slideshow.resume);
        Weather weather = new Weather(slideshow.weather);

        slideshow.canvas.setPreferredSize(screenSize);
        slideshow.canvas.setSize(screenSize);

        JFrame root = new JFrame("Slideshow");
        root.setUndecorated(true);
        root.setBounds(0, 0, screenSize.width, screenSize.height);

        JPanel all = (JPanel) root.getContentPane();
        all.setPreferredSize(screenSize);
        all.setLayout(null);
        all.add(slideshow.canvas);

        root.pack();
        root.setResizable(false);
        root.setVisible(true);

        slideshow.startup();
    }

}
