package com.guykn.setsolver.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.guykn.setsolver.ImageFileManager;

/**
 * Anything that can be drawn on a canvas implements this interface.
 */
public interface DrawableOnCanvas { //todo: find a better class name
    String TAG = "DrawingDebug";
    public default void drawOnCanvas(Canvas canvas){
        drawOnCanvas(canvas, new Paint());
    };
    public void drawOnCanvas(Canvas canvas, Paint paint);
    public void saveToGallery(ImageFileManager fileManager, Bitmap originalImage);

}