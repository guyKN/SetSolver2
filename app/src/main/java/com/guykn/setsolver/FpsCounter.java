package com.guykn.setsolver;

import android.hardware.Camera;
import android.os.SystemClock;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.threading.CameraAction;

//todo: finish
public class FpsCounter implements CameraAction {



    private final FpsCallback callback;
    private final long fpsUpdateTimeInterval;

    private long lastTime;
    private long numFrames;

    public FpsCounter(FpsCallback callback, long fpsUpdateTimeInterval){
        this.callback = callback;
        this.fpsUpdateTimeInterval = fpsUpdateTimeInterval;
    }

    @Override
    public void onCameraStarted() {
        lastTime = getElapsedRealTime();
        numFrames = 0;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
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

    @Override
    public void configureCamera(Camera camera) {

    }

    @Override
    public void onCameraStopped() {

    }

    @Override
    public void setConfig(ImageProcessingConfig config) {

    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

    }

    private static long getElapsedRealTime(){
        return SystemClock.elapsedRealtime();
    }

    public interface FpsCallback{
        public void displayFps(int fps);
    }



}
