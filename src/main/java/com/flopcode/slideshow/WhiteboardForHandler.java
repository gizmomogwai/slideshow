package com.flopcode.slideshow;

import mindroid.os.Handler;

public class WhiteboardForHandler {
    private final Whiteboard whiteboard;
    private final Handler handler;

    public WhiteboardForHandler(Whiteboard whiteboard, Handler handler) {
        this.whiteboard = whiteboard;
        this.handler = handler;
    }

    public WhiteboardForHandler add(String key, WhiteboardForHandler.Observer observer) {
        whiteboard.add(key, new Whiteboard.Observer() {
            @Override
            public void valueChanged(String key, Object value) {
                observer.valueChanged(key, value);
            }

            @Override
            public Handler getHandler() {
                return handler;
            }
        });
        return this;
    }

    public interface Observer {
        void valueChanged(String key, Object value);
    }
}


