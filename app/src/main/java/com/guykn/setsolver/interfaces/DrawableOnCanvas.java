package com.guykn.setsolver.interfaces;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Anything that can be drawn on a canvas implements this interface.
 */
public interface DrawableOnCanvas {
    public default Canvas drawOnCanvas(Canvas canvas){
        return drawOnCanvas(canvas, new Paint());
    };
    public Canvas drawOnCanvas(Canvas canvas, Paint paint);

}
