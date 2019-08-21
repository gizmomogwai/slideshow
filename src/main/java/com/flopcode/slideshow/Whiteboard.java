package com.flopcode.slideshow;


import mindroid.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Whiteboard {

    public interface Observer {
        void valueChanged(String key, Object value);
        Handler getHandler();
    }

    Map<String, Object> data = new HashMap<>();

    Map<String, List<Observer>> observers = new HashMap<>();

    public synchronized Whiteboard add(String key, Observer observer) {
        List<Observer> current = observers.get(key);
        if (current == null) {
            current = new ArrayList<>();
        }
        current.add(observer);
        observers.put(key, current);
        observer.getHandler().post(() -> observer.valueChanged(key, data.get(key)));
        return this;
    }

    public synchronized void set(String key, Object value) {
        data.put(key, value);

        List<Observer> listOfObservers = observers.get(key);
        if (listOfObservers == null) {
            return;
        }

        for (Observer o : listOfObservers) {
            o.getHandler().post(() -> o.valueChanged(key, value));
        }
    }

    public synchronized Object get(String key) {
        return data.get(key);
    }
}
