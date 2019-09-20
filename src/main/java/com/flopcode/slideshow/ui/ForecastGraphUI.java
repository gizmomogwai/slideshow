package com.flopcode.slideshow.ui;

import com.flopcode.slideshow.processes.Weather;

import java.awt.Graphics2D;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.summarizingInt;

public class ForecastGraphUI implements UI {
    private final Supplier<List<Weather.Forecast>> forecastSupplier;

    public ForecastGraphUI(Supplier<List<Weather.Forecast>> forecast) {
        this.forecastSupplier = forecast;
    }

    static class FromTo {
        private final int from;
        private final int to;

        public FromTo(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    @Override
    public void render(Gfx gfx, Graphics2D g) throws Exception {
        List<Weather.Forecast> forecasts = forecastSupplier.get();
        if (forecasts.size() == 0) {
            return;
        }

        IntSummaryStatistics minMax = forecasts.stream().collect(summarizingInt((f) -> f.temperature));

        int WIDTH = 400;
        int HEIGHT = 100;
        gfx.centerString("" + minMax.getMax(), WIDTH / 2, -5);
        gfx.drawRect(0, 0, WIDTH, HEIGHT);
        gfx.centerString("" + minMax.getMin(), WIDTH / 2, HEIGHT + 20);

        {
            List<Integer> temperatures = forecasts.stream().map((a) -> a.temperature).collect(Collectors.toList());
            double dx = WIDTH / (double) temperatures.size();
            gfx.curveFromTo((int) 0, HEIGHT - mapTo(temperatures.get(0), minMax, HEIGHT),
                    (int) 0, HEIGHT - mapTo(temperatures.get(0), minMax, HEIGHT),
                    (int) dx, HEIGHT - mapTo(temperatures.get(1), minMax, HEIGHT),
                    (int) 2 * dx, HEIGHT - mapTo(temperatures.get(2), minMax, HEIGHT)
            );
            for (int i = 1; i < temperatures.size() - 2; i++) {
                gfx.curveFromTo((int) ((i - 1) * dx), HEIGHT - mapTo(temperatures.get(i - 1), minMax, HEIGHT),
                        (int) (i * dx), HEIGHT - mapTo(temperatures.get(i), minMax, HEIGHT),
                        (int) ((i + 1) * dx), HEIGHT - mapTo(temperatures.get(i + 1), minMax, HEIGHT),
                        (int) ((i + 2) * dx), HEIGHT - mapTo(temperatures.get(i + 2), minMax, HEIGHT)
                );
            }
            gfx.curveFromTo((int) (dx * (temperatures.size() - 3)), HEIGHT - mapTo(temperatures.get(temperatures.size() - 2), minMax, HEIGHT),
                    (int) (dx * (temperatures.size() - 2)), HEIGHT - mapTo(temperatures.get(temperatures.size() - 1), minMax, HEIGHT),
                    (int) dx * (temperatures.size() - 1), HEIGHT - mapTo(temperatures.get(temperatures.size() - 1), minMax, HEIGHT),
                    (int) dx * (temperatures.size() - 1), HEIGHT - mapTo(temperatures.get(temperatures.size() - 1), minMax, HEIGHT)
            );
        }
    }

    private int mapTo(int from, IntSummaryStatistics minMax, int width) {
        return (int) ((from - minMax.getMin()) / (double) (minMax.getMax() - minMax.getMin()) * width);
    }
}
