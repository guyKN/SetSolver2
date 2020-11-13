package com.guykn.setsolver;

import android.os.SystemClock;

public class DelayCounter {
    private long startTime;
    private long endTime;

    public void startCounting(){
        startTime = getElapsedRealTime();
    }

    public long stopCounting(){
        endTime = getElapsedRealTime();
        return endTime - startTime;
    }

    public long getTimeCost(){
        return endTime - startTime;
    }

    private static long getElapsedRealTime(){
        return SystemClock.elapsedRealtime();
    }
}
