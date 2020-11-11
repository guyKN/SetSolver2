package com.guykn.setsolver.threading;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

import static com.guykn.setsolver.threading.CameraThread.CameraState.ACTIVE;
import static com.guykn.setsolver.threading.CameraThread.CameraState.DESTROYED;
import static com.guykn.setsolver.threading.CameraThread.CameraState.STOPPED;

public abstract class CameraThread extends HandlerThread implements SurfaceHolder.Callback {

    private static final String TAG = "CameraThread";
    private ImageProcessingConfig config;
    private CameraState lifecycleBasedMaxCameraState;
    private CameraState targetCameraState;
    private CameraState maxPossibleCameraState;
    private Handler handler;
    private SurfaceViewState surfaceViewState;
    private CameraExceptionCallback exceptionCallback;

    public enum CameraState {
        DESTROYED(0),
        STOPPED(1),
        ACTIVE(2);

        final private int id;

        CameraState(int id) {
            this.id = id;
        }

        public boolean isAtLeast(CameraState targetState) {
            return this.id >= targetState.id;
        }

        private static final CameraState[] allStates = {DESTROYED, STOPPED, ACTIVE};

        private static CameraState fromId(int id) {
            return allStates[id];
        }

        public static CameraState lowestState(CameraState state1, CameraState state2) {
            int lowestStateId = Math.min(state1.id, state2.id);
            return fromId(lowestStateId);
        }

        public static CameraState lowestState(CameraState state1, CameraState state2, CameraState state3) {
            int lowestStateId = Math.min(
                    Math.min(state1.id, state2.id),
                    state3.id);
            return fromId(lowestStateId);
        }


    }


    public static class SurfaceViewState {
        private final SurfaceHolder holder;

        private final int format;
        private final int width;
        private final int height;

        public SurfaceViewState(SurfaceHolder holder, int format, int width, int height) {
            this.holder = holder;
            this.format = format;
            this.width = width;
            this.height = height;
        }

        public SurfaceHolder getHolder() {
            return holder;
        }

        public int getFormat() {
            return format;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }


    public CameraThread(String name, CameraExceptionCallback exceptionCallback,
                        ImageProcessingConfig config) {
        super(name);
        this.exceptionCallback = exceptionCallback;
        this.config = config;

        targetCameraState = DESTROYED;
        maxPossibleCameraState = DESTROYED;
        lifecycleBasedMaxCameraState = ACTIVE;
    }

    public void setConfig(ImageProcessingConfig config){
        this.config = config;
        onConfigChanged(config);
    }

    public void setTargetPreviewState(CameraState targetState) {
        changeCameraState(targetState,
                this.maxPossibleCameraState,
                this.lifecycleBasedMaxCameraState);
    }


    private void setMaxPossibleCameraState(CameraState maxPossibleState) {
        changeCameraState(this.targetCameraState,
                maxPossibleState,
                this.lifecycleBasedMaxCameraState);
    }

    public void setLifecycleBasedMaxCameraState(CameraState lifecycleBasedMaxCameraState){
        changeCameraState(this.targetCameraState,
                this.maxPossibleCameraState,
                lifecycleBasedMaxCameraState);
    }

    public void takePicture(){
        try{
            onTakePicture();
        } catch (CameraException e) {
            onCameraError(e);
        }
    }

    private void changeCameraState(CameraState targetState,
                                   CameraState maxPossibleState,
                                   CameraState lifecycleBasedState) {
        Log.d(TAG, "changeCameraState() called");
        CameraState oldCameraState = getActualCameraState();
        Log.d(TAG, "old targetState: " + this.targetCameraState +
                "\nold MaxPossibleState: " + this.maxPossibleCameraState +
                "\nold actualState: " + oldCameraState);
        this.targetCameraState = targetState;
        this.maxPossibleCameraState = maxPossibleState;
        this.lifecycleBasedMaxCameraState = lifecycleBasedState;
        CameraState newCameraState = getActualCameraState();
        Log.d(TAG, "new targetState: " + targetState +
                "\nnew MaxPossibleState: " + maxPossibleState +
                "\nnew actualState: " + newCameraState);

        try {

            switch (newCameraState) {
                case DESTROYED:
                    switch (oldCameraState) {
                        case DESTROYED:
                            break;
                        case STOPPED:
                            onDestroyCamera();
                            break;
                        case ACTIVE:
                            onStopCamera();
                            onDestroyCamera();
                            break;
                    }
                    break;

                case STOPPED:
                    switch (oldCameraState) {
                        case DESTROYED:
                            onOpenCamera();
                            break;
                        case STOPPED:
                            break;
                        case ACTIVE:
                            onStopCamera();
                            break;
                    }
                    break;
                case ACTIVE:
                    switch (oldCameraState) {
                        case DESTROYED:
                            onOpenCamera();
                            onStartCamera(surfaceViewState);
                            break;
                        case STOPPED:
                            onStartCamera(surfaceViewState);
                            break;
                        case ACTIVE:
                            break;
                    }
                    break;
            }
        } catch (CameraException e) {
            onCameraError(e);
        }
    }

    protected void onCameraError(@Nullable CameraException exception) {
        if (exception != null) {
            exception.printStackTrace();
            exceptionCallback.onException(exception);
        }else {
            exceptionCallback.onException();
        }

        try {
            onStopCamera();
        } catch (CameraException ignored) {
            //ignored. We already know an error was happening, so there's no need to do anything.
        }
        try {
            onDestroyCamera();
        } catch (CameraException ignored) {
            //ignored. We already know an error was happening, so there's no need to do anything.
        }
        targetCameraState = DESTROYED;
    }

    private void updateCameraSurface(){
        // stop the cameraPreview, then start it with the new surfaceHolder.
        setMaxPossibleCameraState(STOPPED);
        setMaxPossibleCameraState(ACTIVE);
    }


    @Nullable
    public Handler getHandler() {
        if (handler == null) {
            Looper looper = getLooper();
            if (looper == null) {
                return null;
            }
            handler = new Handler(looper);
        }
        return handler;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
            setMaxPossibleCameraState(STOPPED);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        surfaceViewState = new SurfaceViewState(holder, format, width, height);
        updateCameraSurface();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        surfaceViewState = null;
        setMaxPossibleCameraState(DESTROYED);
    }

    private CameraState getActualCameraState() {
        return CameraState.lowestState(targetCameraState, maxPossibleCameraState, lifecycleBasedMaxCameraState);
    }

    protected ImageProcessingConfig getConfig(){
        return config;
    }

    protected abstract void onOpenCamera() throws CameraException;

    protected abstract void onStartCamera(SurfaceViewState surfaceViewState) throws CameraException;

    protected abstract void onStopCamera() throws CameraException;

    protected abstract void onDestroyCamera() throws CameraException;

    protected abstract void onTakePicture() throws CameraException;

    protected abstract void onConfigChanged(ImageProcessingConfig config);

    public static class CameraException extends Exception {
        public CameraException() {
        }

        public CameraException(String message) {
            super(message);
        }

        public CameraException(String message, Throwable cause) {
            super(message, cause);
        }

        public CameraException(Throwable cause) {
            super(cause);
        }
    }

    public interface CameraExceptionCallback {
        public void onException();
        public void onException(@NonNull CameraException exception);
        public void onException(String message);
    }


}
