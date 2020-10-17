package com.guykn.setsolver.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.guykn.setsolver.drawing.DrawableOnCanvas;

public class SetCardOutlineView extends View {

    private DrawableOnCanvas drawable;
    private Paint mPaint;

    public SetCardOutlineView(Context context, @Nullable AttributeSet attrs) {
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
        this.drawable = drawable;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(drawable == null) return;
        drawable.drawOnCanvas(canvas, mPaint);
    }
}
