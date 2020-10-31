package com.guykn.setsolver.threading;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.guykn.setsolver.FpsCounter;
import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.imageprocessing.image.JpegByteArrayImage;
import com.guykn.setsolver.ui.views.CameraPreview;

import org.opencv.core.Mat;

@SuppressWarnings("deprecation")
public class CameraPreviewThread extends HandlerThread implements Camera.PictureCallback,
        SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String THREAD_NAME = "CameraPreviewThread";

    private static final String TAG = CameraPreview.TAG;

    private Camera mCamera;
    private Handler mHandler;

    private static final int CAMERA_RESTART_DELAY = 0;

    @Nullable
    private final ImageFileManager fileManager;
    @Nullable
    private final FpsCounter fpsCounter;

    @Nullable
    protected ImageFileManager getFileManager(){
        return fileManager;
    }

    public CameraPreviewThread(Context context , @Nullable FpsCounter fpsCounter) {
        super(THREAD_NAME);
        this.fileManager = new ImageFileManager(context);
        this.fpsCounter = fpsCounter;
    }

    public CameraPreviewThread(@Nullable ImageFileManager fileManager,
                               @Nullable FpsCounter fpsCounter){
        super(THREAD_NAME);
        this.fileManager = fileManager;
        this.fpsCounter = fpsCounter;
    }
    

    protected Camera getCamera(){
        return mCamera;
    }

    protected Handler getHandler(){
        if(mHandler == null){
            mHandler = new Handler(getLooper());
        }
        return mHandler;
    }

    private void internalSurfaceCreated(@NonNull SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        Log.d(TAG, "surfaceCreated(). ");
        openCamera();
    }

    private void internalSurfaceChanged(@NonNull SurfaceHolder holder, int format, int w, int h) {
        Log.d(TAG, "surfaceChanged.");
        if (holder.getSurface() == null){
            // preview surface does not exist
            return;
        }
        stopCamera();
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        startCamera();
    }

    private void internalSurfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed(). ");
        destroyCamera();
    }

    private void internalTerminateThread(){
        onTerminateThread();
        quit();
    }



    protected void onTerminateThread(){

    }

    protected void openCamera(){
        Log.d(TAG, "openCamera()");
        mCamera = getCameraInstance();
        if(mCamera == null) {
            Log.w(TAG, "couldn't open camera");
        }
    }

    protected void startCamera(){
        Log.d(TAG, "startCamera()");
        try {
            mCamera.startPreview();
            mCamera.autoFocus(null);
            mCamera.setPreviewCallback(this);
            fpsCounter.startCounting();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void stopCamera(){
        Log.d(TAG, "stopCamera()");
        mCamera.stopPreview();
        mCamera.cancelAutoFocus();
        mCamera.setPreviewCallback(null);
        fpsCounter.startCounting();
    }

    protected void destroyCamera(){
        Log.d(TAG, "destroyCamera()");
        if(mCamera == null){
            return;
        }
        stopCamera();
        try {
            mCamera.release();
        }catch (Exception e){
            e.printStackTrace();
            // ignore: tried to stop a non-existent preview
        }
        mCamera = null;
    }



    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        fpsCounter.onFrame();
    }

    @Override
    public final void onPictureTaken(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPictureSize();
        int width = size.width;
        int height = size.height;

        JpegByteArrayImage image = new JpegByteArrayImage(data, width, height);
        pictureTakenMatAction(image.toMat());
        stopCamera();
        //startCamera();
        //startCameraDelayed();
    }

    protected void pictureTakenMatAction(Mat mat){
        if(fileManager != null){
            fileManager.saveToGallery(mat);
        }
    }

    private void internalTakePicture(){
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

    /**
     * After a certain amount of time, start the camera again.
     * Used to give the user a certain amount of time to look at the camera output
     * before it starts again
     */
    private void startCameraDelayed(){
        getHandler().postDelayed(
                () -> {
                    if(mCamera != null) {
                        startCamera();
                    }
                },
                CAMERA_RESTART_DELAY
        );
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        getHandler().post(
                ()->internalSurfaceCreated(holder)
        );
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        getHandler().post(
                ()->internalSurfaceChanged(holder, format ,width, height)
        );
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        getHandler().post(
                ()->internalSurfaceDestroyed(holder)
        );
    }

    public void takePicture(){
        getHandler().post(
                ()->internalTakePicture()
        );
    }

    public void terminateThread(){
        getHandler().post(this::internalTerminateThread);
    }
}
