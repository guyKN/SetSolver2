package com.guykn.setsolver.threading;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.guykn.setsolver.threading.CameraThread.CameraState.ACTIVE;
import static com.guykn.setsolver.threading.CameraThread.CameraState.DESTROYED;
import static com.guykn.setsolver.threading.CameraThread.CameraState.STOPPED;

public abstract class CameraThread extends HandlerThread implements SurfaceHolder.Callback {

    private static final String TAG = "CameraThread";
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

        public boolean isAtLeast(CameraState targetState){
            return this.id>=targetState.id;
        }

        private static final CameraState[] allStates = {DESTROYED, STOPPED, ACTIVE};

        private static CameraState fromId(int id){
            return allStates[id];
        }

        public static CameraState lowestState(CameraState state1, CameraState state2){
            int lowestStateId = Math.min(state1.id, state2.id);
            return fromId(lowestStateId);
        }
    }



    protected static class SurfaceViewState {
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

    private CameraState targetCameraState;
    private CameraState maxPossibleCameraState;

    public CameraThread(String name, CameraExceptionCallback exceptionCallback) {
        super(name);
        this.exceptionCallback = exceptionCallback;

        targetCameraState = DESTROYED;
        maxPossibleCameraState = DESTROYED;

    }

    public void setTargetPreviewState(CameraState newTargetState){
        targetCameraState = newTargetState;
        if(surfaceViewState == null){
            // the surfaceHolder that this CameraThread is attached to is currntly destroyed
            // this is mostly likely because the activity is paused,
            // or because the surfaceview hasn't been created yet.
            // We can just change our target state for now, and when the surface holder is created
            // our new state will apply
            return;
        }
    }



    @Nullable
    public Handler getHandler() {
        if (handler == null) {
            Looper looper = getLooper();
            if(looper == null) {
                return null;
            }
            handler = new Handler(looper);
        }
        return handler;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if(targetCameraState.isAtLeast(STOPPED)) {
            onOpenCamera();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        surfaceViewState = new SurfaceViewState(holder, format, width, height);

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        surfaceViewState = null;
        if(targetCameraState.isAtLeast(ACTIVE)) {
            onStopCamera();
        }
        if(targetCameraState.isAtLeast(STOPPED)) {
            onDestroyCamera();
        }
    }

    private CameraState getActualCameraState(){
        return CameraState.lowestState(targetCameraState, maxPossibleCameraState);
    }

    protected abstract void onOpenCamera() throws CameraException;


    protected abstract void onStartCamera(SurfaceViewState surfaceViewState) throws CameraException;

    protected abstract void onStopCamera() throws CameraException;

    protected abstract void onDestroyCamera() throws CameraException;


    public static class CameraException extends Exception {
        public CameraException() { }

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

    public interface CameraExceptionCallback{
        public void onException(CameraException exception);
    }
}
