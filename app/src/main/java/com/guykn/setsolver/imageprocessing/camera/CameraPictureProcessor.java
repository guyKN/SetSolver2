package com.guykn.setsolver.imageprocessing.camera;

import android.content.Context;
import android.hardware.Camera;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager;
import com.guykn.setsolver.imageprocessing.JavaImageProcessingManager;
import com.guykn.setsolver.imageprocessing.camera.CameraFrameProcessor.Callback;
import com.guykn.setsolver.imageprocessing.classify.models.v2floatmodels.CardClassifierV2.CardClassifierV2Factory;
import com.guykn.setsolver.imageprocessing.image.Image;
import com.guykn.setsolver.imageprocessing.image.JpegByteArrayImage;
import com.guykn.setsolver.set.SetBoardPosition;
import com.guykn.setsolver.threading.CameraAction;
import com.guykn.setsolver.threading.CameraThread;


//todo: maybe just simplify the code a bit by putting everything into the cameraPreview Class
@SuppressWarnings("deprecation")
public class CameraPictureProcessor implements CameraAction {

    private final JavaImageProcessingManager.Builder processingManagerBuilder;
    private final Callback callback;

    private ImageProcessingManager processingManager;
    private ImageProcessingConfig config;

    public CameraPictureProcessor(Callback callback,
                                  Context context,
                                  ImageProcessingConfig config) {
        this.callback = callback;
        processingManagerBuilder = new JavaImageProcessingManager.Builder(context);
        processingManagerBuilder.setClassifierFactory(new CardClassifierV2Factory());
        this.config = config;
    }

    @Override
    public void onCameraStarted(Camera camera, CameraThread.SurfaceViewState surfaceViewState) {

    }

    @Override
    public void onCameraStopped() {

    }

    @Override
    public void setConfig(ImageProcessingConfig config) {
        this.config = config;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        processingManager = processingManagerBuilder.build(config);
        Camera.Size cameraSize = camera.getParameters().getPictureSize();
        int width = cameraSize.width;
        int height = cameraSize.height;
        Image byteArrayImage = new JpegByteArrayImage(data, width, height);
        processingManager.setImage(byteArrayImage);
        SetBoardPosition cardLocations =  processingManager.getBoard();
        callback.onImageProcessingSuccess(cardLocations);
        processingManager.finish();
    }
}