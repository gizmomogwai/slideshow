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
package com.flopcode.slideshow.database;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class GoodImages {
    final java.util.List<DatabaseImage> allImages;
    java.util.List<DatabaseImage> goodImages;
    LocalDate now;
    int index;

    GoodImages(java.util.List<DatabaseImage> allImages) {
        this.allImages = allImages;
        calcGoodImages(allImages);
    }

    private void calcGoodImages(List<DatabaseImage> allImages) {
        now = LocalDate.now();
        List<DatabaseImage> goodImages = allImages
                .stream()
                .filter(i -> sameMonth(now, i))
                .collect(Collectors.toList());
        goodImages.sort(new DatabaseImageComparator(now));
        this.goodImages = goodImages;
        this.index = 0;
    }

    private boolean sameMonth(LocalDate d, DatabaseImage i) {
        return i.creationData.getMonth() == d.getMonth();
    }

    public DatabaseImage next(Database database, List<Database.Listener> listeners) {
        if (!now.isEqual(LocalDate.now())) {
            calcGoodImages(allImages);
        }
        DatabaseImage res = goodImages.get(index);
        index = (index + 1) % goodImages.size();
        for (Database.Listener l : listeners) {
            l.databaseChanged(database);
        }
        return res;
    }
}
