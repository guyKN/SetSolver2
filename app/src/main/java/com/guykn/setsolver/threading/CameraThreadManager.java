package com.guykn.setsolver.threading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;

import com.guykn.setsolver.CameraActivity;
import com.guykn.setsolver.imageprocessing.detect.ContourBasedCardDetector;
import com.guykn.setsolver.ui.main.CameraFragment;

public class CameraThreadManager extends ImageProcessingThreadManager implements Camera.PreviewCallback {
    private DelayChecker delayChecker;
    public CameraThreadManager(Context context, Callback callback,
                               ContourBasedCardDetector.Config config, DelayChecker delayChecker) {
        super(context, callback, config);
        this.delayChecker = delayChecker;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //Log.d(CameraFragment.TAG, "previewing frame");
        if(delayChecker.shouldStartProcessing()){
            //Log.d(CameraFragment.TAG, "timer told me to go");
            if(!isThreadBusy()){
            //    Log.d(CameraFragment.TAG, "starting thread");
                Camera.Size size = camera.getParameters().getPreviewSize();
                int width = size.width;
                int height = size.height;
                processImage(data, width, height);
                delayChecker.startedProcessing();
            }
        }
    }

    public static interface DelayChecker {
        public boolean shouldStartProcessing();
        public void startedProcessing();
    }

}
