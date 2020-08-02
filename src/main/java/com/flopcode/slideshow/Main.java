package com.flopcode.slideshow;

import com.flopcode.slideshow.clock.Clock;
import com.flopcode.slideshow.clock.RealClock;
import com.flopcode.slideshow.logger.Logger;
import com.flopcode.slideshow.logger.StdoutLogger;
import com.flopcode.slideshow.processes.Database;
import com.flopcode.slideshow.processes.FileScanner;
import com.flopcode.slideshow.processes.MotionDetector;
import com.flopcode.slideshow.processes.Weather;
import mindroid.os.Looper;
import mindroid.os.Message;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static com.flopcode.slideshow.logger.Logger.Level.INFO;

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
        Logger logger = new StdoutLogger(INFO);
        Clock clock = new RealClock();
        Whiteboard whiteboard = new Whiteboard();

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        logger.i("screenSize = " + screenSize);
        //screenSize.width /= 2;
        //screenSize.height /= 2;

        Database db = new Database(logger, clock, whiteboard);
        FileScanner scanner = new FileScanner(logger, db.fileReceiver, args);
        Looper.prepare();
        Slideshow slideshow = new Slideshow(logger, clock, db.imageRequest, whiteboard, screenSize);
        MotionDetector motionDetector = new MotionDetector(logger, slideshow.pause, slideshow.resume);
        Weather weather = new Weather(logger, whiteboard);

        slideshow.canvas.setPreferredSize(screenSize);
        slideshow.canvas.setSize(screenSize);

        JFrame root = new JFrame("Slideshow");
        root.setUndecorated(true);
        root.setBounds(0, 0, screenSize.width, screenSize.height);

        JPanel all = (JPanel) root.getContentPane();
        all.setPreferredSize(screenSize);
        all.setLayout(null);
        all.add(slideshow.canvas);
        all.setFocusable(true);
        all.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case 'd':
                    slideshow.nextStep.sendMessage(new Message());
                    break;
                }
                 /*   case KeyEvent.VK_LEFT:
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        slideshow.nextStep.sendMessage(new Message());
                        break;
                }
                */
            }
        });
        all.requestFocus();

        root.pack();
        root.setResizable(false);
        root.setVisible(true);

        slideshow.startup();
    }

}
