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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;

public class GeoLocationCache {

    private HashMap<URL, String> map = new HashMap<>();
    private String CACHE_FILENAME = "geolocation.cache";
    private int requestedElements = 0;

    GeoLocationCache() {
        load();
    }

    private void load() {
        try {
            ObjectInputStream data = new ObjectInputStream(new FileInputStream(CACHE_FILENAME));
            map = (HashMap<URL, String>) data.readObject();
            data.close();
        } catch (Exception e) {
            map = new HashMap<>();
        }
    }

    private void save() throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(CACHE_FILENAME));
        out.writeObject(map);
        out.close();
    }

    public String get(URL url, Function<URL, String, Exception> getter) {
        if (!map.containsKey(url)) {
            try {
                map.put(url, getter.apply(url));
                requestedElements++;
                if (requestedElements > 0) {
                    try {
                        save();
                        requestedElements = 0;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return map.get(url);
    }

    @FunctionalInterface
    public interface Function<T, R, E extends Throwable> {
        R apply(T t) throws E;
    }
}
