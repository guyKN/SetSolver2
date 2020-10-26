package com.guykn.setsolver.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.guykn.setsolver.drawing.DrawableOnCanvas;

public class CameraOverlay extends View {

    private DrawableOnCanvas drawable;
    private Paint mPaint;

    public CameraOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        drawable = null;
        init();
    }

    private void init(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.YELLOW);
        mPaint.setStrokeWidth(10f);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public void setDrawable(DrawableOnCanvas drawable){
        //todo: do all the math ahead of time, so that the location of the rect doesn't have to be calculated every time
        this.drawable = drawable;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        //todo: do all the math ahead of time, so that the location of the rect doesn't have to be calculated every time

        if(drawable == null) return;
        drawable.drawOnCanvas(canvas, mPaint);
    }
}
