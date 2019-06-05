package com.flopcode.slideshow;

import com.flopcode.slideshow.database.Database;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Toolkit;

public class Main {

    public static void main(String[] args) throws Exception {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("screenSize = " + screenSize);
        // screenSize.width /= 3;
        // screenSize.height /= 3;

        Database db = Database.fromPath(args[0]);

        Slideshow imageList = Slideshow.fromDatabase(db, screenSize);

        Statistics statistics = new Statistics(db);
        statistics.setVisible(false);

        JFrame root = new JFrame("Slideshow");
        root.setUndecorated(true);
        root.setBounds(0, 0, screenSize.width, screenSize.height);

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
