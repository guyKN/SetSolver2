package com.guykn.setsolver;

import android.hardware.Camera;
import android.os.SystemClock;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.threading.CameraThread;

//todo: finish
public class FpsCounter {



    private final FpsCallback callback;
    private long fpsUpdateTimeInterval;

    private long lastTime;
    private long numFrames;

    public FpsCounter(FpsCallback callback, long fpsUpdateTimeInterval){
        this.callback = callback;
        this.fpsUpdateTimeInterval = fpsUpdateTimeInterval;
    }

    public FpsCounter(FpsCallback callback, ImageProcessingConfig config){
        this(callback, config.fpsCounting.fpsUpdateInterval);
    }

    public void onCameraStarted(Camera camera, CameraThread.SurfaceViewState surfaceViewState) {
        lastTime = getElapsedRealTime();
        numFrames = 0;
    }

    public void onFrame() {
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

    public void setConfig(ImageProcessingConfig config) {
        this.fpsUpdateTimeInterval = config.fpsCounting.fpsUpdateInterval;
    }

    private static long getElapsedRealTime(){
        return SystemClock.elapsedRealtime();
    }

    public interface FpsCallback{
        public void displayFps(int fps);
    }



}
