package com.guykn.setsolver.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.List;

public abstract class DrawableList<Drawable extends DrawableOnCanvas> implements DrawableOnCanvas{
    protected List<Drawable> drawables;

    protected void addDrawable(Drawable drawable){
        drawables.add(drawable);
    }

    @Override
    public void drawOnCanvas(Canvas canvas, Paint paint) {
        for(Drawable drawable: drawables){
            drawable.drawOnCanvas(canvas, paint);
        }
    }
}
