package com.flopcode.slideshow;

import org.shredzone.commons.suncalc.MoonIllumination;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Objects;

import static java.awt.Image.SCALE_AREA_AVERAGING;

public class Moon {
    private Phase current;

    Moon() throws Exception {
        BufferedImage images = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("moon_blurred_8.png")));
        current = new Phase(images, 0);
    }

    public Phase getPhase() {
        current = current.revalidateForToday();
        return current;
    }

    public static class Phase {
        public final Image image;
        final double phase;
        private final BufferedImage images;

        Phase(BufferedImage images, double phase) {
            this.images = images;
            this.phase = phase;
            image = cutImage(images, phase);
        }

        /**
         * @param images moon images 6 rows a 4 columns
         * @param phase  -180 .. 180
         * @return image for the requested phase
         */
        private Image cutImage(BufferedImage images, double phase) {
            int index = (int) Math.round(((phase + 180.0) / 360.0) * 24.0);
            int row = index / 4;
            int column = index % 4;
            return images.getSubimage(28 + column * 300, 28 + row * 300, 300, 300).getScaledInstance(50, -1, SCALE_AREA_AVERAGING);
        }

        Phase revalidateForToday() {
            MoonIllumination moonIllumination = MoonIllumination.compute().execute();
            double currentPhase = moonIllumination.getPhase();
            if (Math.abs(currentPhase - phase) < 0.1) {
                return this;
            }
            return new Phase(images, currentPhase);
        }

        public void render(Graphics2D g) {
            g.drawImage(image, 0, 0, null);
        }

    }
}
