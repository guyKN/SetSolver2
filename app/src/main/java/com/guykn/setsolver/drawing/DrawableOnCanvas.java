package com.guykn.setsolver.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.guykn.setsolver.ImageFileManager;

import org.opencv.core.Mat;
import org.opencv.core.Size;

/**
 * Anything that can be drawn on a canvas implements this interface.
 */
public interface DrawableOnCanvas { //todo: find a better class name
    String TAG = "DrawingDebug";
    public default void drawOnCanvas(Canvas canvas){
        drawOnCanvas(canvas, new Paint());
    };
    public void drawOnCanvas(Canvas canvas, Paint paint);
    public void saveToGallery(ImageFileManager fileManager, Mat originalImage);
    public void saveToGallery(ImageFileManager fileManager, Mat originalImage, Size scaledDownSize);

}