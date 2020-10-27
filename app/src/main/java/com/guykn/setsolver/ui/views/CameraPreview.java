package com.guykn.setsolver.ui.views;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.threading.CameraProcessingThread;

/**
 * This class extends the SurfaceView class, and simply forwards oll SurfaceHolder Callbacks to CameraPreviewThread
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        LifecycleObserver {

    public static final String TAG = "CameraPreviewTag";

    private CameraProcessingThread processingThread;

    public CameraPreview(Context context, CameraProcessingThread processingThread, Lifecycle lifecycle) {
        super(context);

        lifecycle.addObserver(this);

        this.processingThread = processingThread;
        processingThread.start();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(){
        Log.d(TAG, "onResume() called inside of CameraPreview");
        resumeCamera();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause(){
        Log.d(TAG, "onPause() called inside of CameraPreview");
        stopCamera();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(){
        processingThread.quit(); //todo: add built in method
    }


    // All calls regarding the surfaceHolder are called in the UI thread,
    // so we first have to forward them to the worker thread

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        processingThread.getHandler().post(
                ()->processingThread.surfaceCreated(holder)
        );
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        processingThread.getHandler().post(
                ()->processingThread.surfaceChanged(holder, format, width, height)
        );
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        processingThread.getHandler().post(
                ()->processingThread.surfaceDestroyed(holder)
        );
    }

    public void startCamera(){
        processingThread.getHandler().post(
                ()->processingThread.startCamera()
        );
    }

    public void resumeCamera(){
        processingThread.getHandler().post(
                ()->processingThread.resumeCamera()
        );
    }

    public void pauseCamera(){
        processingThread.getHandler().post(
                ()->processingThread.pauseCamera()
        );
    }

    public void stopCamera(){
        processingThread.getHandler().post(
                ()->processingThread.stopCamera()
        );
    }

    public void setConfig(ImageProcessingConfig config){
        processingThread.getHandler().post(
                ()->processingThread.setConfig(config)
        );
    }

    public void takePicture() {
        processingThread.getHandler().post(
                ()->processingThread.takePicture()
        );
    }
}
