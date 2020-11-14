package com.guykn.setsolver.threading;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.SystemClock;

import com.guykn.setsolver.FpsCounter;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.camera.CameraFrameProcessor;
import com.guykn.setsolver.imageprocessing.camera.CameraPictureProcessor;
import com.guykn.setsolver.ui.main.MainViewModel;

import java.io.IOException;
import java.util.List;

import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
import static com.guykn.setsolver.threading.CameraPreviewThread.CameraUiState.CAMERA_ACTIVE;
import static com.guykn.setsolver.threading.CameraPreviewThread.CameraUiState.CAMERA_CURRENTLY_PROCESSING;
import static com.guykn.setsolver.threading.CameraPreviewThread.CameraUiState.CAMERA_DONE_PROCESSING;
import static com.guykn.setsolver.threading.CameraPreviewThread.CameraUiState.CAMERA_INACTIVE;
import static com.guykn.setsolver.threading.CameraPreviewThread.CameraUiState.CAMERA_LOADING;

@SuppressWarnings("deprecation")
public class CameraPreviewThread extends CameraThread implements Camera.PreviewCallback,
        Camera.PictureCallback {

    private static final String THREAD_NAME = "CameraPreviewThread";
    private Camera camera;
    private MainViewModel mainViewModel;
    private final FpsCounter fpsCounter;
    private final CameraFrameProcessor frameProcessor;
    private final CameraPictureProcessor pictureProcessor;

    private final String TAG = "CameraPreviewThread";
    private boolean isLoading;

    @Override
    protected void onConfigChanged(ImageProcessingConfig config) {
        fpsCounter.setConfig(config);
    }

    public CameraPreviewThread(Context context,
                               MainViewModel mainViewModel,
                               ImageProcessingConfig config) {
        super(THREAD_NAME, mainViewModel, config);
        this.mainViewModel = mainViewModel;
        fpsCounter = new FpsCounter(mainViewModel, config);

        frameProcessor = new CameraFrameProcessor(mainViewModel, context, config);
        pictureProcessor = new CameraPictureProcessor(mainViewModel, context, config);
    }

    @Override
    protected void onOpenCamera() throws CameraException {
        try {
            mainViewModel.setCameraUiState(CAMERA_LOADING);
            camera = Camera.open();
            if (camera == null) {
                throw new CameraException("Couldn't open Camera");
            }
        } catch (RuntimeException e) {
            throw new CameraException(e);
        }
    }

    @Override
    protected void onStartCamera(SurfaceViewState surfaceViewState) throws CameraException {
        try {
            if (surfaceViewState == null) {
                throw new CameraException(
                        "Method onStartCamera() was given a null surfaceViewState");
            }

            frameProcessor.onCameraStarted(camera, surfaceViewState);
            mainViewModel.setCameraUiState(CAMERA_LOADING);
            Camera.Parameters parameters = camera.getParameters();

            //todo: ensure no exception is thrown, based on: https://developer.android.com/reference/android/hardware/Camera.Parameters#setPreviewSize(int,%20int)
            Size bestPreviewSize = findBestSize(surfaceViewState.getWidth(),
                    surfaceViewState.getHeight(),
                    parameters.getSupportedPreviewSizes());
            parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

            Size bestPictureSize = findBestSize(surfaceViewState.getWidth(),
                    surfaceViewState.getHeight(),
                    parameters.getSupportedPictureSizes());
            parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);


            parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE); //todo: ensure setting is supported

            camera.setParameters(parameters);

            camera.setPreviewDisplay(surfaceViewState.getHolder());
            surfaceViewState.getHolder().setKeepScreenOn(true);
            camera.startPreview();
            camera.autoFocus(null);
            camera.setPreviewCallback(this);
        } catch (IOException | RuntimeException e) {
            throw new CameraException(e);
        }
    }

    @Override
    protected void onStopCamera() throws CameraException {
        try {
            if(!isLoading) {
                mainViewModel.setCameraUiState(CAMERA_INACTIVE);
            }
            camera.stopPreview();
            camera.cancelAutoFocus();
            camera.setPreviewCallback(null);
        } catch (RuntimeException e) {
            throw new CameraException(e);
        }
    }

    @Override
    protected void onDestroyCamera() throws CameraException {
        try {
            mainViewModel.setCameraUiState(CAMERA_INACTIVE);
            camera.release();
        } catch (RuntimeException e) {
            throw new CameraException(e);
        }
    }

    public void onTakePicture() {
        try {
            camera.takePicture(null, null, null, this);
        }catch (RuntimeException ignored){
            // Ignored. The user tried to press the shutter button while camera was inactive.
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        isLoading = true;
        try {
            long startTime = SystemClock.elapsedRealtime();

            setTargetPreviewState(CameraState.STOPPED);

            mainViewModel.setCameraUiState(CAMERA_CURRENTLY_PROCESSING);

            pictureProcessor.onPictureTaken(data, camera);

            long endTime = SystemClock.elapsedRealtime();
            mainViewModel.setTotalProcessingTime(endTime - startTime);
            mainViewModel.setCameraUiState(CAMERA_DONE_PROCESSING);
            isLoading = false;
        }catch (RuntimeException e){
            isLoading = false;
            onCameraError(new CameraException(e));
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        try {
            fpsCounter.onFrame();
            mainViewModel.setCameraUiState(CAMERA_ACTIVE); //todo: don't do this every frame
            frameProcessor.onPreviewFrame(data, camera);
        }catch (RuntimeException e){
            onCameraError(new CameraException(e));
        }
    }

    private static Size findBestSize(int previewWidth, int previewHeight,
                                     List<Size> sizes) {

        double targetAspectRatio = getAspectRatio(previewWidth, previewHeight);

        Size currentBestSize = null;
        double currentLowestError = 10000; // arbitrary large number
        for (Size size : sizes) {
            double currentAspectRatio = getAspectRatio(size);
            double currentError = Math.abs(currentAspectRatio - targetAspectRatio);
            if (currentError < currentLowestError) {
                currentBestSize = size;
                currentLowestError = currentError;
            }
        }
        return currentBestSize;
    }

    private static double getAspectRatio(int width, int height) {
        return ((double) width) / ((double) height);
    }

    private static double getAspectRatio(Size size) {
        return getAspectRatio(size.width, size.height);
    }

    public enum CameraUiState{
        CAMERA_INACTIVE,
        CAMERA_LOADING,
        CAMERA_ACTIVE,
        CAMERA_CURRENTLY_PROCESSING,
        CAMERA_DONE_PROCESSING
    }
}