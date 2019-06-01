/*
 * Copyright (c) 2019 E.S.R.Labs. All rights reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of E.S.R.Labs and its suppliers, if any.
 * The intellectual and technical concepts contained herein are
 * proprietary to E.S.R.Labs and its suppliers and may be covered
 * by German and Foreign Patents, patents in process, and are protected
 * by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from E.S.R.Labs.
 */
package com.flopcode.slideshow;

import com.flopcode.slideshow.Main.DatabaseImage;

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
