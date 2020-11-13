package com.guykn.setsolver.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.guykn.setsolver.drawing.DrawingCallback;


public class CameraOverlay extends View implements LifecycleObserver {

    private static final String TAG = DrawingCallback.TAG;
    private DrawingCallback drawable;
    private Paint mPaint;

    public CameraOverlay(Context context, Lifecycle lifecycle) {
        super(context);
        drawable = null;
        init();
        lifecycle.addObserver(this);
    }

    private void init(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.YELLOW);
        mPaint.setStrokeWidth(10f);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public void setDrawable(DrawingCallback drawable){
        //todo: do all the math ahead of time, so that the location of the rect doesn't have to be calculated every time
        this.drawable = drawable;
        drawable.onSizeChange(getWidth(), getHeight());
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(drawable != null){
            drawable.onSizeChange(getWidth(), getHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        //todo: do all the math ahead of time, so that the location of the rect doesn't have to be calculated every time

        if(drawable == null) return;

        drawable.drawOnCanvas(canvas, mPaint);
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause(){
        setVisibility(GONE);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(){
        setVisibility(VISIBLE);
    }

}
