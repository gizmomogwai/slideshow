package com.flopcode.slideshow.data.images;

import com.drew.lang.GeoLocation;

import java.io.Serializable;
import java.time.LocalDate;

public final class DateAndLocation implements Serializable {
    private LocalDate date;
    private Location location;

    public DateAndLocation(LocalDate date, GeoLocation location) {
        this.setDate(date);
        if (location != null) {
            this.setLocation(new Location(location.getLatitude(), location.getLongitude()));
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
