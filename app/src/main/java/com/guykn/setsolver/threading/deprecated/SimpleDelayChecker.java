package com.guykn.setsolver.threading.deprecated;

import com.guykn.setsolver.threading.deprecated.CameraThreadManager;

public class SimpleDelayChecker implements CameraThreadManager.DelayChecker {
    private long processingDelay;
    private long checkDelay;

    private long lastCheckTime;
    private long lastProcessingTime;

    public SimpleDelayChecker(long processingDelay, long checkDelay){
        this.processingDelay = processingDelay;
        this.checkDelay = checkDelay;
    }

    @Override
    public boolean shouldStartProcessing() {
        long now = System.currentTimeMillis();
        if(now - lastProcessingTime >= processingDelay && now - lastCheckTime >= checkDelay) {
            lastCheckTime = now;
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void startedProcessing() {
        lastProcessingTime = System.currentTimeMillis();
    }
}
