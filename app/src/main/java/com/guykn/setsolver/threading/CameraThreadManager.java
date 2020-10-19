package com.guykn.setsolver.threading;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import com.guykn.setsolver.MainActivity;
import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.imageprocessing.Config;

public class CameraThreadManager extends ImageProcessingThreadManager implements Camera.PreviewCallback {
    private DelayChecker delayChecker;
    public CameraThreadManager(Context context, Callback callback,
                               DelayChecker delayChecker) {
        super(context, callback);
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
                Log.i(GenericRotatedRectangle.TAG, "very original aspect ratio: " + ((float)width)/((float)height));
                new UiToWorkerThreadByteArrayMessage(ImageProcessingAction.DETECT_CARDS,
                        Config.getDefaultConfig(),data, width, height)
                            .send();
                Log.i(MainActivity.TAG, "Message sent. ");
                delayChecker.startedProcessing();
            }
        }
    }

    public static interface DelayChecker {
        public boolean shouldStartProcessing();
        public void startedProcessing();
    }

}
