package com.flopcode.slideshow.data.images;

import com.drew.lang.GeoLocation;

import java.io.Serializable;
import java.time.LocalDate;

public final class DateAndLocation implements Serializable {
    public LocalDate date;
    public Location location;

    public DateAndLocation(LocalDate date, GeoLocation location) {
        this.date = date;
        if (location != null) {
            this.location = new Location(location.getLatitude(), location.getLongitude());
        }
    }
}
