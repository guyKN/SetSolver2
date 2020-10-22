package com.guykn.setsolver.threading;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager;
import com.guykn.setsolver.imageprocessing.image.ByteArrayImage;
import com.guykn.setsolver.ui.main.CameraFragment;

import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class CameraPreviewThreadManager implements SurfaceHolder.Callback {
    //todo: handle exceptions and errors better, add on onCameraErrorListener
    //todo: ensure that you are opening back facing camera
    //todo: loop through resolutions and find the one closest to the actual preview, for better sync

    private Handler uiToWorkerThreadHandler;
    private CameraPreviewThread mCameraPreviewThread;

    public CameraPreviewThreadManager(){
        mCameraPreviewThread = new CameraPreviewThread();
        mCameraPreviewThread.start();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if(uiToWorkerThreadHandler == null){
            return;
        }
        uiToWorkerThreadHandler.post(()->
            mCameraPreviewThread.surfaceCreated(holder)
        );
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if(uiToWorkerThreadHandler == null){
            return;
        }
        uiToWorkerThreadHandler.post(()->
                mCameraPreviewThread.surfaceChanged(holder, format, width, height)
        );
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if(uiToWorkerThreadHandler == null){
            return;
        }
        uiToWorkerThreadHandler.post(()->
                mCameraPreviewThread.surfaceDestroyed(holder)
        );

    }

    private class CameraPreviewThread extends Thread implements SurfaceHolder.Callback, Camera.PreviewCallback{

        private Camera mCamera;
        private ImageProcessingManager manager;
        @Override
        public void run() {
            manager = ImageProcessingManager.getDefaultManager(
                    ImageProcessingConfig.getDefaultConfig());

            Looper.prepare();
            uiToWorkerThreadHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
            Looper.loop();
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            Log.d(CameraFragment.TAG, "surfaceCreated()");
            mCamera = getCameraInstance();
            if(mCamera == null) {
                Log.w(CameraFragment.TAG, "couldn't open camera");
            }
        }

        @Override
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

        @Override
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
                mCamera.setPreviewCallback(this);//todo: implement CameraPreviewCallback
                mCamera.startPreview();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size imageSize = camera.getParameters().getPictureSize();
            int width = imageSize.width;
            int height = imageSize.height;
            ByteArrayImage byteImage = new ByteArrayImage(data, width, height);
            manager.setImage(byteImage);
            RotatedRectangleList cardPositions = manager.getCardPositions();
            manager.finish();
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
}
