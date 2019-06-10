package com.flopcode.slideshow;

import com.flopcode.slideshow.database.Database;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Toolkit;

public class Main {

    public static void main(String[] args) throws Exception {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("screenSize = " + screenSize);
//         screenSize.width /= 3;
//         screenSize.height /= 3;

        Database db = Database.fromPath(args[0]);

        Slideshow slideshow = Slideshow.fromDatabase(db, screenSize);
        slideshow.setPreferredSize(screenSize);
        slideshow.setSize(screenSize);

        JFrame root = new JFrame("Slideshow");
        root.setUndecorated(true);
        root.setBounds(0, 0, screenSize.width, screenSize.height);

        JPanel all = (JPanel) root.getContentPane();
        all.setPreferredSize(screenSize);
        all.setLayout(null);
        all.add(slideshow);

        root.pack();
        root.setResizable(false);
        root.setVisible(true);

        slideshow.run();
    }

}
