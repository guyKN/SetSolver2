package com.guykn.setsolver.interfaces;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Anything that can be drawn on a canvas implements this interface.
 */
public interface DrawableOnCanvas {
    public default void drawOnCanvas(Canvas canvas){
        drawOnCanvas(canvas, new Paint());
    };
    public void drawOnCanvas(Canvas canvas, Paint paint);

}