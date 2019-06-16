package com.flopcode.slideshow.database;

import java.time.LocalDate;
import java.util.Comparator;

public class DatabaseImageComparator implements Comparator<DatabaseImage> {
    private final LocalDate date;

    public DatabaseImageComparator(LocalDate date) {
        this.date = date;
    }

    @Override
    public int compare(DatabaseImage i1, DatabaseImage i2) {
        int match1 = i1.creationData.getDayOfMonth() - date.getDayOfMonth();
        int match2 = i2.creationData.getDayOfMonth() - date.getDayOfMonth();
        return match1 - match2;
    }
}
