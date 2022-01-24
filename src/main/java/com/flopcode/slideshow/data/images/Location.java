package com.flopcode.slideshow.data.images;

import java.io.Serializable;

public final class Location implements Serializable {
    public final double latitude;
    public final double longitute;

    public Location(double latitude, double longitute) {
        this.latitude = latitude;
        this.longitute = longitute;
    }
}
