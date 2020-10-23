package com.guykn.setsolver.ui.views;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.threading.CameraProcessingThread;

/**
 * This class extends the SurfaceView class, and simply forwards oll SurfaceHolder Callbacks to CameraPreviewThread
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private CameraProcessingThread processingThread;

    public CameraPreview(Context context, CameraProcessingThread processingThread) {
        super(context);
        this.processingThread = processingThread;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        processingThread.start();
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

    public void setConfig(ImageProcessingConfig config){
        processingThread.getHandler().post(
                ()->processingThread.setConfig(config)
        );
    }
}
