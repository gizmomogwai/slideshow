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

import com.flopcode.slideshow.database.DatabaseImage;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.time.LocalDate;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

public class SlideshowImage {
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
        g2d.setColor(Color.white);
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
