    package com.guykn.setsolver.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.guykn.setsolver.FpsCounter;
import com.guykn.setsolver.drawing.DrawingCallback;
import com.guykn.setsolver.threading.CameraThreadManager;

    public class MainViewModel extends ViewModel implements CameraThreadManager.CameraProcessingThread.Callback,
        FpsCounter.FpsCallback {

    private final MutableLiveData<DrawingCallback> drawingLiveData;

    private final MutableLiveData<Integer> frameRateData;

    public MainViewModel(){
        super();
        drawingLiveData = new MutableLiveData<>();
        frameRateData = new MutableLiveData<>();
    }

    public void setDrawable(DrawingCallback drawable){
        drawingLiveData.setValue(drawable);
    }
    public void postDrawable(DrawingCallback drawable){
        drawingLiveData.postValue(drawable);
    }

    public LiveData<DrawingCallback> getDrawableLiveData(){
        return drawingLiveData;
    }

    public LiveData<Integer> getFpsLiveData(){
        return frameRateData;
    }

    public void displayFps(int frameRate) {
        frameRateData.postValue(frameRate);
    }

    @Override
    public void onImageProcessingSuccess(DrawingCallback drawable) {
        postDrawable(drawable);
    }

    @Override
    public void onImageProcessingFailure(Exception exception) {
        exception.printStackTrace();
        //todo: tell the UI thread something went wrong
    }
}