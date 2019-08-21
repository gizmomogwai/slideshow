package com.flopcode.slideshow.ui;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class Gfx {
    final Graphics2D graphics;

    public Gfx(Graphics2D graphics, int x, int y, int width, int height) {
        this.graphics = graphics;
        this.graphics.setClip(x, y, width, height);
    }

    public void setColor(Color color) {
        graphics.setColor(color);
    }

    public void fillRect(int x, int y, int width, int height) {
        graphics.fillRect(x, y, width, height);
    }

    public void dispose() {
        graphics.dispose();
    }

    public void setRenderingHint(RenderingHints.Key key, Object value) {
        graphics.setRenderingHint(key, value);
    }

    public void setComposite(Composite composite) {
        graphics.setComposite(composite);
    }

    public void render(UI ui, int x, int y) {
        AffineTransform store = graphics.getTransform();
        graphics.translate(x, y);
        try {
            ui.render(this, graphics);
        } catch (Exception e) {
            e.printStackTrace();
        }
        graphics.setTransform(store);
    }

    public int fromRight(int x) {
        return graphics.getClipBounds().width - x;
    }

    public int fromRight(double x) {
        return fromRight((int) x);
    }

    public int fromTop(int y) {
        return y;
    }

    public void setFont(Font font) {
        graphics.setFont(font);
    }

    public void drawImage(Image i, int x, int y) {
        graphics.drawImage(i, x, y, null);
    }

    public Rectangle2D getStringBounds(String s) {
        return graphics.getFontMetrics().getStringBounds(s, graphics);
    }

    public void drawString(String s, int x, int y) {
        graphics.drawString(s, x, y);
    }

    public void drawStringRightAligned(String s, int x, int y) {
        Rectangle2D size = getStringBounds(s);
        graphics.drawString(s, (int) (x - size.getWidth()), y);
    }

    public void centerString(String s, int x, int y) {
        Rectangle2D size = getStringBounds(s);
        graphics.drawString(s, (int) (x - size.getWidth() / 2), y);
    }

    public int fromBottom(int y) {
        return graphics.getClipBounds().height - y;
    }
}
