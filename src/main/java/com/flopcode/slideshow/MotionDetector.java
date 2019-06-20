package com.flopcode.slideshow;

import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

public class MotionDetector extends HandlerThread {

    private final Handler mPause;
    private final Handler mResume;
    private Handler activate;

    public MotionDetector(Handler pause, Handler resume) {
        mPause = pause;
        mResume = resume;
        start();
        activate = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    Process process = new ProcessBuilder("xset", "dpms", "force", "on").start();
                    int res = process.waitFor();
                    if (res != 0) {
                        System.out.println("MotionDetector.handleMessage - xset dpms force on -> " + res);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendMessageDelayed(new Message(), 50000);
                msg.recycle();
            }
        };
        activate.sendMessage(new Message());
    }
}
