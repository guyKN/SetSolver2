package com.guykn.setsolver.ui.views;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.guykn.setsolver.FpsCounter;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager;
import com.guykn.setsolver.threading.CameraPreviewThread;
import com.guykn.setsolver.threading.CameraProcessingThread;

/**
 * This class extends the SurfaceView class, and simply forwards oll SurfaceHolder Callbacks to CameraPreviewThread
 */
public class CameraPreview extends SurfaceView implements LifecycleObserver {

    //todo: make the class more generic


    public static final String TAG = "CameraPreviewTag";

    private final CameraPreviewThread previewThread;


    public CameraPreview(Context context, ImageProcessingManager processingManager,
                         @Nullable CameraProcessingThread.Callback processingCallback,
                         @Nullable FpsCounter fpsCounter,
                         @Nullable Lifecycle lifecycle) {
        super(context);

        if(lifecycle != null){
            lifecycle.addObserver(this);
        }
        this.previewThread = new CameraProcessingThread(context, fpsCounter,
                processingManager, processingCallback);

        init();
    }

    public CameraPreview(Context  context,
                         CameraPreviewThread previewThread, Lifecycle lifecycle){
        super(context);
        if(lifecycle != null){
            lifecycle.addObserver(this);
        }
        this.previewThread = previewThread;
        init();
    }


    private void init(){
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        SurfaceHolder holder = getHolder();
        holder.addCallback(previewThread);
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //make it so that the screen never turns off while this view is visible
        setKeepScreenOn(true);

        previewThread.start();
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(){
        setVisibility(VISIBLE);

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause(){
        setVisibility(GONE);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(){
        previewThread.terminateThread();
    }


    // All calls regarding the surfaceHolder are called in the UI thread,
    // so we first have to forward them to the worker thread



    public void setConfig(ImageProcessingConfig config){

    }

    public void takePicture() {
        previewThread.takePicture();
    }
}
