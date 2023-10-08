package com.flopcode.slideshow.processes;

import com.flopcode.slideshow.Constants;
import com.flopcode.slideshow.logger.Logger;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import mindroid.os.Handler;
import mindroid.os.HandlerThread;
import mindroid.os.Message;

/**
 * Triggers on/off every 50s depending on the last motion detector events
 */
public class MotionDetector extends HandlerThread {

    private static final int ON = 1;
    private static final int OFF = 0;
    private final Handler mPause;
    private final Handler mResume;
    private Handler activate;
    private Logger logger;

    public MotionDetector(Logger logger, Handler pause, Handler resume) {
        this.logger = logger;
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
                        logger.i("MotionDetector.handleMessage - xset dpms force on -> " + res);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendMessageDelayed(new Message().setWhat(msg.what), Constants.REFRESH_DISPLAY);
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
            Context pi4j = Pi4J.newAutoContext();

            DigitalInputConfigBuilder gpio = DigitalInput
                    .newConfigBuilder(pi4j)
                    .id("motion detector")
                    .name("motion detector")
                    .address(4)
                    .pull(PullResistance.PULL_DOWN)
                    .debounce(3000L)
                    .provider("pigpio-digital-input");
            DigitalInput motion = pi4j.create(gpio);
            motion.addListener(event -> {
                logger.d(" --> GPIO PIN STATE CHANGE: " + event.source() + " = " + event.state());
                handleState(event.state());
            });
            handleState(motion.state());
        } catch (Exception e) {
            e.printStackTrace();
            logger.e("Cannot initialize gpios");
        }
    }

    private Message createKeepOnMessage() {
        return new Message().setWhat(ON);
    }

    private void handleState(DigitalState state) {
        activate.removeMessages(OFF);
        activate.removeMessages(ON);
        if (state == DigitalState.HIGH) {
            logger.d("MotionDetector.MotionDetector - keeping on");
            activate.sendMessage(createKeepOnMessage());
        } else {
            logger.d("MotionDetector.MotionDetector - killing keep on");
            activate.sendMessage(createSwitchOffMessage());
        }
    }

    private Message createSwitchOffMessage() {
        return new Message().setWhat(OFF);
    }
}
