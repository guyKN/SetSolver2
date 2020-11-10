package com.guykn.setsolver.threading;

import android.hardware.Camera;

import java.io.IOException;

@SuppressWarnings("deprecation")
public class CameraPreviewThread extends CameraThread {
    //todo: handle errors in the camera with a callback
    private static final String THREAD_NAME = "CameraPreviewThread";
    private Camera camera;

    private final CameraAction previewAction;

    public CameraPreviewThread(CameraExceptionCallback exceptionCallback,
                               CameraAction previewAction) {
        super(THREAD_NAME, exceptionCallback);
        this.previewAction = previewAction;
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
            previewAction.onCameraStarted(camera, surfaceViewState);
            if(surfaceViewState == null){
                throw new CameraException(
                        "Method onStartCamera() was given a null surfaceViewState");
            }
            camera.setPreviewDisplay(surfaceViewState.getHolder());
            camera.startPreview();
            camera.autoFocus(null);
            camera.setPreviewCallback(previewAction);
        } catch (IOException | RuntimeException e) {
            throw new CameraException(e);
        }
    }

    @Override
    protected void onStopCamera() throws CameraException {
        try {
            previewAction.onCameraStopped();
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
        camera.takePicture(null, null, null, previewAction);
    }

}
