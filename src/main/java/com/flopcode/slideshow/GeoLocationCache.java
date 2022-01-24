package com.flopcode.slideshow;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;

public class GeoLocationCache {

    private HashMap<URL, String> map = new HashMap<>();
    private final String CACHE_FILENAME = "geolocation.cache";
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
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(CACHE_FILENAME))) {
            out.writeObject(map);
        }
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
