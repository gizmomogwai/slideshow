package com.flopcode.slideshow.data.moon;

import org.shredzone.commons.suncalc.MoonIllumination;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static java.awt.Image.SCALE_AREA_AVERAGING;
import static java.util.Objects.requireNonNull;
import static javax.imageio.ImageIO.read;

public class Moon {
    private Phase current;

    public Moon() throws IOException {
        BufferedImage images = read(requireNonNull(getClass().getClassLoader().getResourceAsStream("moon_blurred_8.png")));
        current = new Phase(images, 0);
    }

    public Phase getPhase() {
        current = current.revalidateForToday();
        return current;
    }

    public static class Phase {
        public final Image image;
        final double phaseInGrad;
        private final BufferedImage images;

        Phase(BufferedImage images, double phaseInGrad) {
            this.images = images;
            this.phaseInGrad = phaseInGrad;
            image = cutImage(images, phaseInGrad);
        }

        /**
         * @param images moon images 6 rows a 4 columns
         * @param phaseInGrad  -180 .. 180
         * @return image for the requested phaseInGrad
         */
        private Image cutImage(BufferedImage images, double phaseInGrad) {
            int index = (int) Math.round(((phaseInGrad + 180.0) / 360.0) * 24.0);
            int row = index / 4;
            int column = index % 4;
            return images.getSubimage(28 + column * 300, 28 + row * 300, 300, 300).getScaledInstance(50, -1, SCALE_AREA_AVERAGING);
        }

        Phase revalidateForToday() {
            MoonIllumination moonIllumination = MoonIllumination.compute().execute();
            double currentPhase = moonIllumination.getPhase();
            if (Math.abs(currentPhase - phaseInGrad) < 0.1) {
                return this;
            }
            return new Phase(images, currentPhase);
        }

        public void render(Graphics2D g) {
            g.drawImage(image, 0, 0, null);
        }

    }
}
