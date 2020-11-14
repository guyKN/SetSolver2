package com.guykn.setsolver.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.guykn.setsolver.FpsCounter;
import com.guykn.setsolver.drawing.DrawingCallback;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.camera.CameraFrameProcessor;
import com.guykn.setsolver.threading.CameraPreviewThread.CameraUiState;
import com.guykn.setsolver.threading.CameraThread;

import static com.guykn.setsolver.threading.CameraPreviewThread.CameraUiState.CAMERA_INACTIVE;

public class MainViewModel extends ViewModel implements CameraThread.CameraExceptionCallback,
        FpsCounter.FpsCallback, CameraFrameProcessor.Callback {

    private final MutableLiveData<DrawingCallback> drawingLiveData = new MutableLiveData<>();

    private final MutableLiveData<Integer> frameRateData = new MutableLiveData<>();

    private final MutableLiveData<String> cameraExceptionData = new MutableLiveData<>();

    private final MutableLiveData<ImageProcessingConfig> configLiveData = new MutableLiveData<>();

    private final MutableLiveData<CameraUiState> cameraUiStateLiveData = new MutableLiveData<>();

    private final MutableLiveData<Long> totalProcessingTimeData = new MutableLiveData<>();

    public MainViewModel() {
        super();
        configLiveData.setValue(ImageProcessingConfig.getDefaultConfig());
        cameraUiStateLiveData.setValue(CAMERA_INACTIVE);
    }

    public void setDrawable(DrawingCallback drawable) {
        drawingLiveData.setValue(drawable);
    }

    public void postDrawable(DrawingCallback drawable) {
        drawingLiveData.postValue(drawable);
    }



    public void setConfig(ImageProcessingConfig config){
        configLiveData.postValue(config);
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

    public LiveData<ImageProcessingConfig> getConfigLiveData(){
        return configLiveData;
    }

    public void setCameraUiState(CameraUiState cameraUiState){
        cameraUiStateLiveData.postValue(cameraUiState);
    }

    public void setTotalProcessingTime(long totalProcessingTime){
        totalProcessingTimeData.postValue(totalProcessingTime);
    }

    public LiveData<Long> getTotalProcessingTimeData(){
        return totalProcessingTimeData;
    }

    public LiveData<CameraUiState> getCameraUiStateLiveData(){
        return cameraUiStateLiveData;
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

    @Override
    public void onImageProcessingSuccess(DrawingCallback drawable) {
        postDrawable(drawable);
    }


}