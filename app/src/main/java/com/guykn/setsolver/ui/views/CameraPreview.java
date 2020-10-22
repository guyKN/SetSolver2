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
import com.guykn.setsolver.ui.main.CameraFragment;

import java.io.IOException;
import java.util.List;

/**
 * This class extends the SurfaceView class, and simply forwards oll SurfaceHolder Callbacks to CameraPreviewThreadManager
 */
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView {
    public CameraPreview(Context context, SurfaceHolder.Callback callback) {
        super(context);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        SurfaceHolder holder = getHolder();
        holder.addCallback(callback);
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }



}
