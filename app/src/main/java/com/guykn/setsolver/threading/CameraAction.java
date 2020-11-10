package com.guykn.setsolver.threading;

import android.hardware.Camera;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

public interface CameraAction extends Camera.PreviewCallback, Camera.PictureCallback {
    public void onCameraStarted(Camera camera, CameraThread.SurfaceViewState surfaceViewState);
    public void onCameraStopped();
    public void setConfig(ImageProcessingConfig config);
}