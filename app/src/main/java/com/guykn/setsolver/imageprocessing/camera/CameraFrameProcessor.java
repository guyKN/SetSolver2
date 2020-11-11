package com.guykn.setsolver.imageprocessing.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import com.guykn.setsolver.drawing.DrawingCallback;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager.ImageProcessingManagerBuilder;
import com.guykn.setsolver.imageprocessing.JavaImageProcessingManager;
import com.guykn.setsolver.imageprocessing.image.ByteArrayImage;
import com.guykn.setsolver.imageprocessing.image.Image;
import com.guykn.setsolver.threading.CameraAction;
import com.guykn.setsolver.threading.CameraThread;

import static com.guykn.setsolver.imageprocessing.JavaImageProcessingManager.TAG;

@SuppressWarnings("deprecation")
public class CameraFrameProcessor implements CameraAction {

    private final ImageProcessingManagerBuilder processingManagerBuilder;
    private final Callback callback;

    private ImageProcessingManager processingManager;
    private ImageProcessingConfig config;

    public CameraFrameProcessor(Callback callback,
                                Context context,
                                ImageProcessingConfig config) {
        this.callback = callback;
        processingManagerBuilder = new JavaImageProcessingManager.Builder(context);
        this.config = config;
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
        Camera.Size cameraSize = camera.getParameters().getPreviewSize();
        int width = cameraSize.width;
        int height = cameraSize.height;
        Image byteArrayImage = new ByteArrayImage(data, width, height);
        processingManager.setImage(byteArrayImage);
        RotatedRectangleList cardLocations =  processingManager.getCardPositions();
        callback.onImageProcessingSuccess(cardLocations);
        Log.d(TAG, "onPreviewFrame called from CameraProcessingAction");
        processingManager.finish();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

    }

    public interface Callback {
        public void onImageProcessingSuccess(DrawingCallback drawable);
    }
}