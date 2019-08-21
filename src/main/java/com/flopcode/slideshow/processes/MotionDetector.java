package com.flopcode.slideshow.processes;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

public class MotionDetector extends HandlerThread {

    private static final int ON = 1;
    private static final int OFF = 0;
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
                    String onOff = handleIncomingMessage(msg);
                    Process process = new ProcessBuilder("xset", "dpms", "force", onOff).start();
                    int res = process.waitFor();
                    if (res != 0) {
                        System.out.println("MotionDetector.handleMessage - xset dpms force on -> " + res);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // loop every 50 seconds
                sendMessageDelayed(new Message().setWhat(msg.what), 50000);
                msg.recycle();
            }

            private String handleIncomingMessage(Message msg) {
                if (msg.what == ON) {
                    mResume.sendEmptyMessage(0);
                    return "on";
                }
                mPause.sendEmptyMessage(0);
                return "off";
            }
        };

        try {
            final GpioController gpio = GpioFactory.getInstance();
            final GpioPinDigitalInput motion = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);
            motion.addListener((GpioPinListenerDigital) event -> {
                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
                handleState(event.getState());
            });
            handleState(motion.getState());

        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("Cannot initialize gpios");
        }
    }

    private Message createKeepOnMessage() {
        return new Message().setWhat(ON);
    }

    private void handleState(PinState state) {
        activate.removeMessages(OFF);
        activate.removeMessages(ON);
        if (state == PinState.HIGH) {
            System.out.println("MotionDetector.MotionDetector - keeping on");
            activate.sendMessage(createKeepOnMessage());
        } else {
            System.out.println("MotionDetector.MotionDetector - killing keep on");
            activate.sendMessage(createSwitchOffMessage());
        }
    }

    private Message createSwitchOffMessage() {
        return new Message().setWhat(OFF);
    }
}
