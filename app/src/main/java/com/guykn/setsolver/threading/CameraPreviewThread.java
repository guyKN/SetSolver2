package com.guykn.setsolver.threading;

import android.content.Context;
import android.hardware.Camera;

import com.guykn.setsolver.FpsCounter;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.camera.CameraFrameProcessor;
import com.guykn.setsolver.imageprocessing.camera.CameraPictureProcessor;
import com.guykn.setsolver.ui.main.MainViewModel;

import java.io.IOException;

@SuppressWarnings("deprecation")
public class CameraPreviewThread extends CameraThread implements Camera.PreviewCallback,
        Camera.PictureCallback {

    private static final String THREAD_NAME = "CameraPreviewThread";
    private Camera camera;
    private MainViewModel mainViewModel;
    private final FpsCounter fpsCounter;
    private final CameraFrameProcessor frameProcessor;
    private final CameraPictureProcessor pictureProcessor;

    @Override
    protected void onConfigChanged(ImageProcessingConfig config) {
        fpsCounter.setConfig(config);
    }

    public CameraPreviewThread(Context context,
                               MainViewModel mainViewModel,
                               ImageProcessingConfig config) {
        super(THREAD_NAME, mainViewModel, config);
        this.mainViewModel = mainViewModel;
        fpsCounter = new FpsCounter(mainViewModel, config);

        frameProcessor = new CameraFrameProcessor(mainViewModel, context, config);
        pictureProcessor = new CameraPictureProcessor(mainViewModel, context, config);
    }

    @Override
    protected void onOpenCamera() throws CameraException {
        try {
            camera = Camera.open();
            if(camera == null){
                throw new CameraException("Couldn't open Camera");
            }
        }catch (RuntimeException e){
            throw new CameraException(e);
        }
    }

    @Override
    protected void onStartCamera(SurfaceViewState surfaceViewState) throws CameraException{
        try {
            if(surfaceViewState == null){
                throw new CameraException(
                        "Method onStartCamera() was given a null surfaceViewState");
            }

            frameProcessor.onCameraStarted(camera, surfaceViewState);


            camera.setPreviewDisplay(surfaceViewState.getHolder());
            surfaceViewState.getHolder().setKeepScreenOn(true);
            camera.startPreview();
            camera.autoFocus(null);
            camera.setPreviewCallback(this);
        } catch (IOException | RuntimeException e) {
            throw new CameraException(e);
        }
    }

    @Override
    protected void onStopCamera() throws CameraException {
        try {
            camera.stopPreview();
            camera.cancelAutoFocus();
            camera.setPreviewCallback(null);
        }catch (RuntimeException e){
            throw new CameraException(e);
        }
    }

    @Override
    protected void onDestroyCamera() throws CameraException {
        try {
            camera.release();
        }catch (RuntimeException e){
            throw new CameraException(e);
        }
    }

    public void onTakePicture(){
        camera.takePicture(null, null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        setTargetPreviewState(CameraState.STOPPED);
        pictureProcessor.onPictureTaken(data, camera);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        fpsCounter.onFrame();
        frameProcessor.onPreviewFrame(data, camera);
    }
}