package com.flopcode.slideshow;

import java.util.HashMap;
import java.util.Map;

public class Whiteboard {

    Map<String, Object> data = new HashMap<>();

    public synchronized void set(String key, Object value) {
        data.put(key, value);
    }

    public synchronized Object get(String key) {
        return data.get(key);
    }
}
