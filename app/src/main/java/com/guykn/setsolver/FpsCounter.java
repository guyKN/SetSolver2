package com.guykn.setsolver;

import android.os.SystemClock;

//todo: finish
public class FpsCounter {



    private final FpsCallback callback;
    private final long fpsUpdateTimeInterval;

    private long lastTime;
    private long numFrames;

    public FpsCounter(FpsCallback callback, long fpsUpdateTimeInterval){
        this.callback = callback;
        this.fpsUpdateTimeInterval = fpsUpdateTimeInterval;
    }

    public void startCounting(){
        lastTime = getElapsedRealTime();
        numFrames = 0;
    }

    public void onFrame(){
        numFrames++;
        long currentTime = getElapsedRealTime();
        long deltaTime = currentTime - lastTime;
        if(deltaTime > fpsUpdateTimeInterval){
            int fps =  (int)((1000 * numFrames) / (deltaTime));
            callback.displayFps(fps);
            numFrames = 0;
            lastTime = currentTime;
        }
    }

    public void stopCounting(){

    }

    private static long getElapsedRealTime(){
        return SystemClock.elapsedRealtime();
    }

    public interface FpsCallback{
        public void displayFps(int fps);
    }



}
