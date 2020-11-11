package com.guykn.setsolver.threading;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.ui.views.CameraPreview2;

public class CameraThreadManager implements LifecycleObserver, SurfaceHolder.Callback {
    private final CameraThread cameraThread;
    private final Lifecycle lifecycle;
    private CameraPreview2 cameraPreview;

    public CameraThreadManager(CameraThread cameraThread, Lifecycle lifecycle) {
        this.cameraThread = cameraThread;
        this.lifecycle = lifecycle;
        this.lifecycle.addObserver(this);
        cameraThread.start();
    }

    public CameraPreview2 getCameraPreview(Context context){
        if(cameraPreview == null){
            synchronized (this){
                if(cameraPreview == null){
                    cameraPreview = new CameraPreview2(context);
                    cameraPreview.setHolderCallback(this);
                }
            }
        }
        return cameraPreview;
    }

    public void startCamera(){
        cameraThread.getHandler().post(()->{
            cameraThread.setTargetPreviewState(CameraThread.CameraState.ACTIVE);
        });
    }

    public void stopCamera(){
        cameraThread.getHandler().post(()->{
            cameraThread.setTargetPreviewState(CameraThread.CameraState.DESTROYED);
        });
    }

    public void takePicture(){
        cameraThread.getHandler().post(cameraThread::takePicture);
    }

    public void setConfig(ImageProcessingConfig config){
        cameraThread.getHandler().post(
                ()->cameraThread.setConfig(config)
        );
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(){
        if(cameraPreview != null){
            cameraPreview.setVisibility(View.VISIBLE);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onPause(){
        if(cameraPreview != null){
            cameraPreview.setVisibility(View.GONE);
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        cameraThread.getHandler().post(()->{
           cameraThread.surfaceCreated(holder);
        });
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        cameraThread.getHandler().post(()->{
            cameraThread.surfaceChanged(holder, format, width, height);
        });
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        cameraThread.getHandler().post(()->{
            cameraThread.surfaceDestroyed(holder);
        });
    }
}