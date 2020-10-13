package com.guykn.setsolver.drawing;

import android.graphics.Canvas;
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