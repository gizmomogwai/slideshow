package com.flopcode.slideshow.database;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class GoodImages {
    final java.util.List<DatabaseImage> allImages;
    java.util.List<DatabaseImage> goodImages;
    LocalDate now;

    GoodImages(java.util.List<DatabaseImage> allImages) {
        this.allImages = allImages;
        calcGoodImages(allImages);
    }

    private void calcGoodImages(List<DatabaseImage> allImages) {
        now = LocalDate.now();
        List<DatabaseImage> goodImages = allImages
                .stream()
             //   .filter(i -> sameMonth(now, i))
                .collect(Collectors.toList());
        goodImages.sort(new DatabaseImageComparator(now));
        this.goodImages = goodImages;
    }

    private boolean sameMonth(LocalDate d, DatabaseImage i) {
        return i.creationData.getMonth() == d.getMonth();
    }

    public DatabaseImage next(Database database) {
        if (!now.isEqual(LocalDate.now())) {
            calcGoodImages(allImages);
        }
        int index = (int) Math.floor(Math.random() * goodImages.size());
        if (index >= goodImages.size()) {
            return null;
        }
        DatabaseImage res = goodImages.get(index);
        database.notifyListeners();
        return res;
    }
}
