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

import com.flopcode.slideshow.database.Database;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Graphics;

public class Statistics extends JComponent {

    private String[] lines;

    public Statistics(Database database) throws Exception {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setForeground(Color.red);
        database.addListener(db -> updateText(db));
    }

    private void setLines(String[] lines) {
        this.lines = lines;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.red);

        int lineHeight = g.getFontMetrics(getFont()).getHeight();
        int h = getInsets().top;
        for (String line : lines) {
            g.drawString(line, getInsets().left, h);
            h += lineHeight;
        }
    }

    private void updateText(Database database) {
        setLines(new String[]{"" +
                "total: " + database.getTotalCount(),
                "selected: " + database.getGoodImageCount(),
        });
    }
}
