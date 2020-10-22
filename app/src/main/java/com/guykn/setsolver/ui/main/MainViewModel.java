package com.guykn.setsolver.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.guykn.setsolver.drawing.DrawableOnCanvas;

public class MainViewModel extends ViewModel  {
    private final MutableLiveData<DrawableOnCanvas> drawingLiveData;

    public MainViewModel(){
        super();
        drawingLiveData = new MutableLiveData<>();
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
}