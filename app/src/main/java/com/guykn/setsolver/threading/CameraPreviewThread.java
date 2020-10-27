package com.guykn.setsolver.threading;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.imageprocessing.image.JpegByteArrayImage;
import com.guykn.setsolver.ui.views.CameraPreview;

import org.opencv.core.Mat;

import java.io.IOException;

//todo: make use handler internally to allow direct calls from UI thread
//todo: improve the lifecycle, make sure it's working

@SuppressWarnings("deprecation")
public class CameraPreviewThread extends HandlerThread implements Camera.PictureCallback{
    
    private static final String TAG = CameraPreview.TAG;
    
    enum CameraPreviewState{
        STOPPED,
        STARTED,
        RESUMED
    }

    private CameraPreviewState mPreviewState;

    private Camera mCamera;
    private Handler mHandler;

    @Nullable
    private ImageFileManager fileManager;

    @Nullable
    protected ImageFileManager getFileManager(){
        return fileManager;
    }

    public CameraPreviewThread(@Nullable ImageFileManager fileManager){
        super("CameraPreviewThread");
        this.fileManager = fileManager;
        mPreviewState = CameraPreviewState.STOPPED;
    }

    public CameraPreviewState getPreviewState(){
        return mPreviewState;
    }

    protected Camera getCamera(){
        return mCamera;
    }

    public Handler getHandler(){
        if(mHandler == null){
            mHandler = new Handler(getLooper());
        }
        return mHandler;
    }

    public final void surfaceCreated(@NonNull SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        Log.d(TAG, "surfaceCreated(). Current state: " + mPreviewState);
        startCamera();
    }

    public final void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed. Current state: " + mPreviewState);
        stopCamera();
    }

    public final void surfaceChanged(@NonNull SurfaceHolder holder, int format, int w, int h) {
        Log.d(TAG, "surfaceChanged. Current state: " + mPreviewState);
        if (holder.getSurface() == null){
            // preview surface does not exist
            return;
        }
        try {
            switch (mPreviewState) {
                case STOPPED:
                    break;
                case STARTED:
                    mCamera.setPreviewDisplay(holder);
                    break;
                case RESUMED:
                    pauseCamera();
                    mCamera.setPreviewDisplay(holder);
                    resumeCamera();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            //todo: handle errors
        }
    }

    public void startCamera(){
        Log.d(TAG, "StartCamera() called. previous state: " + mPreviewState.name());
        switch (mPreviewState){
            case STOPPED:
                onStartCamera();
                break;
            case STARTED:
            case RESUMED:
                new Throwable("Attempted to call startCamera() when Camera was already started")
                        .printStackTrace();
                break;
        }
        mPreviewState = CameraPreviewState.STARTED;
    }

    public void resumeCamera(){
        Log.d(TAG, "resumeCamera() called. previous state: " + mPreviewState.name());
        switch(mPreviewState){
            case STOPPED:
                onStartCamera();
                onResumeCamera();
                break;
            case STARTED:
                onResumeCamera();
                break;
            case RESUMED:
                new Throwable("Attempted to call resumeCamera() when camera was already resumed")
                        .printStackTrace();
                break;
        }
        mPreviewState = CameraPreviewState.RESUMED;
    }

    public void pauseCamera(){
        Log.d(TAG, "pauseCamera() called. previous state: " + mPreviewState.name());
        switch(mPreviewState){
            case STOPPED:
            case STARTED:
                new Throwable("Attempted to call pauseCamera() when camera was already paused")
                        .printStackTrace();
                break;
            case RESUMED:
                onPauseCamera();
                break;
        }
        mPreviewState = CameraPreviewState.STARTED;
    }

    public void stopCamera(){
        Log.d(TAG, "stopCamera() called. previous state: " + mPreviewState.name());
        switch(mPreviewState){
            case STOPPED:
                Log.w(TAG, "Attempted to call stopCamera() when camera was already stopped");
                new Throwable().printStackTrace();
                break;
            case STARTED:
                onPauseCamera();
            case RESUMED:
                onPauseCamera();
                onStopCamera();
        }
        mPreviewState = CameraPreviewState.STOPPED;
    }



    protected void onStartCamera(){
        Log.d(TAG, "onStartCamera()");
        mCamera = getCameraInstance();
        if(mCamera == null) {
            Log.w(TAG, "couldn't open camera");
        }
    }

    protected void onResumeCamera(){
        Log.d(TAG, "onResumeCamera()");
        try {
            mCamera.startPreview();
            mCamera.autoFocus(null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onPauseCamera(){
        Log.d(TAG, "onPauseCamera()");
        mCamera.stopPreview();
    }

    protected void onStopCamera(){
        Log.d(TAG, "onStopCamera()");
        if(mCamera == null){
            return;
        }
        try {
            mCamera.setPreviewCallback(null);
            mCamera.cancelAutoFocus();
            mCamera.release();
        }catch (Exception e){
            e.printStackTrace();
            // ignore: tried to stop a non-existent preview
        }
        mCamera = null;
    }


    @Override
    final public void onPictureTaken(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPictureSize();
        int width = size.width;
        int height = size.height;

        JpegByteArrayImage image = new JpegByteArrayImage(data, width, height);
        pictureTakenMatAction(image.toMat());
    }

    public void pictureTakenMatAction(Mat mat){
        if(fileManager != null){
            fileManager.saveToGallery(mat);
        }
    }

    public void takePicture(){
        mCamera.takePicture(null, null, null, this);
    }

    @Nullable
    private Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

}
