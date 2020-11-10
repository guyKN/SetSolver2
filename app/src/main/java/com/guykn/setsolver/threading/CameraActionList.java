package com.guykn.setsolver.threading;

import android.hardware.Camera;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class CameraActionList extends ArrayList<CameraAction>
        implements CameraAction {

    @Override
    public void configureCamera(Camera camera) {
        for(CameraAction action : this){
            action.configureCamera(camera);
        }
    }

    @Override
    public void onCameraStarted() {
        for(CameraAction action : this){
            action.onCameraStarted();
        }
    }

    @Override
    public void onCameraStopped() {
        for(CameraAction action : this){
            action.onCameraStopped();
        }
    }

    @Override
    public void setConfig(ImageProcessingConfig config) {
        for(CameraAction action : this){
            action.setConfig(config);
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        for(CameraAction action : this){
            action.onPictureTaken(data, camera);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        for(CameraAction action : this){
            action.onPreviewFrame(data, camera);
        }
    }

}
