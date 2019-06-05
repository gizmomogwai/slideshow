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
package com.flopcode.slideshow;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.awt.Image.SCALE_SMOOTH;

public class Main2 {

    static class Slideshow2 extends Canvas {

        private final Image image;

        public Slideshow2(Dimension size, Image scaledImage) {
            image = scaledImage;
            setSize(size);
            setIgnoreRepaint(true);
        }

        public void run() {
            createBufferStrategy(3);
            BufferStrategy strategy = getBufferStrategy();
            int offset = 0;
            while (true) {
                Graphics graphics = strategy.getDrawGraphics();
                graphics.drawImage(image, offset++, 0, null);
                graphics.dispose();
                strategy.show();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame root = new JFrame("Slideshow");
        root.setUndecorated(true);
        root.setBounds(0, 0, screenSize.width, screenSize.height);

        BufferedImage originalImage = ImageIO.read(new File(args[0]));
        Image scaledImage = originalImage.getScaledInstance(-1, screenSize.height, SCALE_SMOOTH);
        Slideshow2 slideshow = new Slideshow2(screenSize, scaledImage);

        JPanel panel = (JPanel) root.getContentPane();
        panel.setLayout(null);
        panel.setPreferredSize(screenSize);
        panel.add(slideshow);
        root.pack();
        root.setResizable(false);
        root.setVisible(true);

        slideshow.run();
    }
}
