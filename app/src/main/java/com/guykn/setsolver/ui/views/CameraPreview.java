package com.guykn.setsolver.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.guykn.setsolver.CameraActivity;
import com.guykn.setsolver.MainActivity;
import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.ui.main.CameraFragment;

import java.io.IOException;

//todo: handle exceptions and errors better
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.PreviewCallback cameraPreviewCallback;

    public CameraPreview(Context context, Camera.PreviewCallback cameraPreviewCallback) {
        super(context);
        this.cameraPreviewCallback = cameraPreviewCallback;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.

        Log.d(GenericRotatedRectangle.TAG,
                "Camera View aspect ratio: " + String.valueOf(((float) getWidth())/((float) getHeight())));


        try {
            mCamera = getCameraInstance();
            if(mCamera == null){
                Log.w(CameraFragment.TAG, "couldn't open camera");
                return;
            }
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if(mCamera == null){
            return;
        }
        mCamera.release();
        mCamera = null;
    }
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
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
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(cameraPreviewCallback);
            mCamera.startPreview();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Nullable
    private Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

}
