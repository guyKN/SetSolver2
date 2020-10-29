package com.guykn.setsolver.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.guykn.setsolver.FpsCounter;
import com.guykn.setsolver.drawing.DrawableOnCanvas;
import com.guykn.setsolver.threading.CameraProcessingThread;

public class MainViewModel extends ViewModel implements CameraProcessingThread.Callback,
        FpsCounter.FpsCallback {

    private final MutableLiveData<DrawableOnCanvas> drawingLiveData;

    private final MutableLiveData<Integer> frameRateData;

    public MainViewModel(){
        super();
        drawingLiveData = new MutableLiveData<>();
        frameRateData = new MutableLiveData<>();
    }

    public void setDrawable(DrawableOnCanvas drawable){
        drawingLiveData.setValue(drawable);
    }
    public void postDrawable(DrawableOnCanvas drawable){
        drawingLiveData.postValue(drawable);
    }

    public LiveData<DrawableOnCanvas> getDrawableLiveData(){
        return drawingLiveData;
    }

    public LiveData<Integer> getFpsLiveData(){
        return frameRateData;
    }

    public void displayFps(int frameRate) {
        frameRateData.postValue(frameRate);
    }

    @Override
    public void onImageProcessingSuccess(DrawableOnCanvas drawable) {
        postDrawable(drawable);
    }

    @Override
    public void onImageProcessingFailure(Exception exception) {
        exception.printStackTrace();
        //todo: tell the UI thread something went wrong
    }
}