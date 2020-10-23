package com.guykn.setsolver.threading.deprecated;

import android.os.Handler;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager;
import com.guykn.setsolver.threading.CameraPreviewThread;
import com.guykn.setsolver.threading.CameraProcessingThread;

@SuppressWarnings("deprecation")
public class CameraPreviewThreadManager implements SurfaceHolder.Callback {
    //todo: handle exceptions and errors better, add on onCameraErrorListener
    //todo: ensure that you are opening back facing camera
    //todo: ensure that no complected thread nonsense prevents camera from being visible
    //todo: loop through resolutions and find the one closest to the actual preview, for better sync

    private Handler uiToWorkerThreadHandler;
    private CameraPreviewThread mCameraPreviewThread;
    private final CameraProcessingThread.Callback mCallback;
    @Nullable
    private final ImageFileManager fileManager;
    private final ImageProcessingManager processingManager;



    public CameraPreviewThreadManager(CameraProcessingThread.Callback callback, @Nullable ImageFileManager fileManager,
                                      ImageProcessingManager processingManager){
        this.processingManager = processingManager;
        this.fileManager = fileManager;
        mCallback = callback;
        mCameraPreviewThread = new CameraPreviewThread(fileManager);
        mCameraPreviewThread.start();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if(uiToWorkerThreadHandler == null){
            return;
        }
        uiToWorkerThreadHandler.post(
                ()-> mCameraPreviewThread.surfaceCreated(holder)
        );
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if(uiToWorkerThreadHandler == null){
            return;
        }
        uiToWorkerThreadHandler.post(
                ()-> mCameraPreviewThread.surfaceChanged(holder, format, width, height)
        );
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if(uiToWorkerThreadHandler == null){
            return;
        }
        uiToWorkerThreadHandler.post(
                ()-> mCameraPreviewThread.surfaceDestroyed(holder)
        );

    }

}
