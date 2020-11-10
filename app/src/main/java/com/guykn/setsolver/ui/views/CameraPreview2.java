package com.guykn.setsolver.ui.views;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview2 extends SurfaceView {
    public CameraPreview2(Context context) {
        super(context);
    }

    public void setHolderCallback(SurfaceHolder.Callback callback){
        SurfaceHolder holder = getHolder();
        holder.addCallback(callback);
    }
}
