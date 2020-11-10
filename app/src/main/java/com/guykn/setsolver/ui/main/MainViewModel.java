package com.guykn.setsolver.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.guykn.setsolver.FpsCounter;
import com.guykn.setsolver.drawing.DrawingCallback;
import com.guykn.setsolver.threading.CameraThread;

public class MainViewModel extends ViewModel implements CameraThread.CameraExceptionCallback,
        FpsCounter.FpsCallback {

    private final MutableLiveData<DrawingCallback> drawingLiveData = new MutableLiveData<>();

    private final MutableLiveData<Integer> frameRateData = new MutableLiveData<>();

    private final MutableLiveData<String> cameraExceptionData = new MutableLiveData<>();

    public MainViewModel() {
        super();
    }

    public void setDrawable(DrawingCallback drawable) {
        drawingLiveData.setValue(drawable);
    }

    public void postDrawable(DrawingCallback drawable) {
        drawingLiveData.postValue(drawable);
    }

    public LiveData<DrawingCallback> getDrawableLiveData() {
        return drawingLiveData;
    }

    public LiveData<Integer> getFpsLiveData() {
        return frameRateData;
    }

    public void displayFps(int frameRate) {
        frameRateData.postValue(frameRate);
    }

    public LiveData<String> getCameraExceptionData(){
        return cameraExceptionData;
    }

    @Override
    public void onException() {
        onException("Something went wrong with the Camera");
    }

    @Override
    public void onException(@NonNull CameraThread.CameraException exception) {
        onException(exception.getMessage());
    }

    @Override
    public void onException(String message) {
        cameraExceptionData.postValue(message);
    }
}