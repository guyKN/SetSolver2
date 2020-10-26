package com.guykn.setsolver.threading;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.imageprocessing.image.JpegByteArrayImage;
import com.guykn.setsolver.ui.main.CameraFragment;

import org.opencv.core.Mat;

import java.util.Objects;

//todo: use HandlerTHread instead of thread

@SuppressWarnings("deprecation")
public class CameraPreviewThread extends Thread implements Camera.PictureCallback{

    @Nullable
    private ImageFileManager fileManager;

    protected ImageFileManager getFileManager(){
        return fileManager;
    }

    public CameraPreviewThread(@Nullable ImageFileManager fileManager){
        this.fileManager = fileManager;
    }

    private Camera mCamera;
    private Handler uiToWorkerThreadHandler;

    public Handler getHandler(){
        return uiToWorkerThreadHandler;
    }
    protected Camera getCamera(){
        return mCamera;
    }

    @Override
    public void run() {
        Looper.prepare();
        uiToWorkerThreadHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        Looper.loop();
    }
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        Log.d(CameraFragment.TAG, "surfaceCreated()");
        mCamera = getCameraInstance();
        if(mCamera == null) {
            Log.w(CameraFragment.TAG, "couldn't open camera");
        }
    }

    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d(CameraFragment.TAG, "onSurfaceDestroyed()");
        if(mCamera == null){
            return;
        }
        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
        }catch (Exception e){
            e.printStackTrace();
            // ignore: tried to stop a non-existent preview
        }
        mCamera = null;
        Log.d(CameraFragment.TAG, "onSurfaceDestroyed() finished");
    }

    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int w, int h) {
        Log.d(CameraFragment.TAG, "surfaceChanged()");
        if (holder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
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
