package com.flopcode.slideshow.ui;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

public class Gfx {
    final Graphics2D graphics;

    public Gfx(Graphics2D graphics, int x, int y, int width, int height) {
        this.graphics = graphics;
        this.graphics.setClip(x, y, width, height);
    }

    public void tmp(Consumer<Gfx> r) {
        Rectangle2D bounds2D = this.graphics.getClip().getBounds2D();
        Gfx g = new Gfx(this.graphics, (int)bounds2D.getX(), (int)bounds2D.getY(), (int)bounds2D.getWidth(), (int)bounds2D.getHeight());
        try {
            r.accept(g);
        } finally {
            g.dispose();
        }
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

    public void drawLine(int x1, int y1, int x2, int y2) {
        graphics.drawLine(x1, y1, x2, y2);
    }

    /*

                x         x

         x                        x

     */
    public void curveFromTo(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x1, y1);
        double cpx1 = x1 + (x2 - x1) * (1 / 3.0);
        double cpx2 = x1 + (x2 - x1) * (2 / 3.0);
        double dy1 = y2 - y0;
        double dy2 = y3 - y1;
        double cpy1 = y1 + (dy1 / 2 / 3.0);
        double cpy2 = y2 - (dy2 / 2 / 3.0);
        /*
        graphics.setColor(Color.RED);
        graphics.drawRect((int) x0 - 1, (int) y0 - 1, 3, 3);
        graphics.drawRect((int) x1 - 1, (int) y1 - 1, 3, 3);
        graphics.drawRect((int) x2 - 1, (int) y2 - 1, 3, 3);
        graphics.drawRect((int) x3 - 1, (int) y3 - 1, 3, 3);
        graphics.setColor(Color.GREEN);
        graphics.drawRect((int) cpx1 - 1, (int) cpy1 - 1, 3, 3);
        graphics.drawRect((int) cpx2 - 1, (int) cpy2 - 1, 3, 3);
*/
        graphics.setColor(Color.WHITE);
        path.curveTo(cpx1, cpy1, cpx2, cpy2, x2, y2);
        graphics.draw(path);
    }


    public void drawRect(int x1, int y1, int x2, int y2) {
        graphics.drawRect(x1, y1, x2 - x1, y2 - y1);
    }

}
