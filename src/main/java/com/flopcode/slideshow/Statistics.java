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
