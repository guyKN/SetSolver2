package com.guykn.setsolver.ui.views;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView {
    public CameraPreview(Context context) {
        super(context);
        setZOrderOnTop(false);
    }

    public void setHolderCallback(SurfaceHolder.Callback callback){
        SurfaceHolder holder = getHolder();
        holder.addCallback(callback);
    }
}
