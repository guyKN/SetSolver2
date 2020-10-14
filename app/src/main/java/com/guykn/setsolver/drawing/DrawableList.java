package com.guykn.setsolver.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.guykn.setsolver.ImageFileManager;

import java.util.ArrayList;
import java.util.List;

public abstract class DrawableList<Drawable extends DrawableOnCanvas> implements DrawableOnCanvas{
    private static final int DEFAULT_TRIM_SIZE = 30;

    protected List<Drawable> drawables = new ArrayList<>();

    public List<Drawable> getDrawables(){
        return drawables;
    }

    protected void addDrawable(Drawable drawable){
        drawables.add(drawable);
    }

    @Override
    public void drawOnCanvas(Canvas canvas, Paint paint) {
        if(drawables == null){
            return;
        }
        for(Drawable drawable: drawables){
            drawable.drawOnCanvas(canvas, paint);
        }
    }

    @Override
    public void saveToGallery(ImageFileManager fileManager, Bitmap originalImage){
        for(DrawableOnCanvas drawable: drawables){
            drawable.saveToGallery(fileManager, originalImage);
        }
    }

    /**
     * Cuts off elements at the end of the list until it is of length maxSize.
     * Used in order to restrict the size of the list in order to avoid the awful lag that may come from high amounts of drawables.
     * Does nothing if the Size of the list is less than maxSize
     * @param maxSize the maximum length that the list is allowed to be.
     */
    public void trimToSize(int maxSize){
        int size = drawables.size();
        if(size <= maxSize){
            return;
        }
        drawables.subList(maxSize, size).clear();
    }
    public void trim(){
        trimToSize(DEFAULT_TRIM_SIZE);
    }
}
