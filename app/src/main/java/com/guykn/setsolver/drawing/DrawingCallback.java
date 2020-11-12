package com.guykn.setsolver.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Anything that can be drawn on a canvas implements this interface.
 */
public interface DrawingCallback {
    String TAG = "DrawingDebug";
    public void drawOnCanvas(Canvas canvas, Paint paint);
    public void onSizeChange(int width, int height);
}