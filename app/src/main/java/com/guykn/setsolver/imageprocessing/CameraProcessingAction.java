package com.guykn.setsolver.imageprocessing;

import android.hardware.Camera;

import com.guykn.setsolver.drawing.DrawingCallback;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager.ImageProcessingManagerBuilder;
import com.guykn.setsolver.threading.CameraAction;
import com.guykn.setsolver.threading.CameraThread;

@SuppressWarnings("deprecation")
public class CameraProcessingAction implements CameraAction {

    private final ImageProcessingManagerBuilder processingManagerBuilder;

    private ImageProcessingManager processingManager;
    private ImageProcessingConfig config;

    public CameraProcessingAction(ImageProcessingManagerBuilder processingManagerBuilder) {
        this.processingManagerBuilder = processingManagerBuilder;
    }

    @Override
    public void onCameraStarted(Camera camera, CameraThread.SurfaceViewState surfaceViewState) {
        processingManager = processingManagerBuilder.build(config);
    }

    @Override
    public void onCameraStopped() {
        processingManager.finish();
        processingManager = null;
    }

    @Override
    public void setConfig(ImageProcessingConfig config) {
        this.config = config;
        if(processingManager != null){
            processingManager.finish();
            processingManager = processingManagerBuilder.build(config);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

    }

    public interface Callback {
        public void onImageProcessingSuccess(DrawingCallback drawable);
    }
}