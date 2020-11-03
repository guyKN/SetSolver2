package com.guykn.setsolver.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;
//todo: maybe just extend the collection class instead of storing a collection inside
public abstract class DrawingCallbackList<Drawable extends DrawingCallback>
        implements DrawingCallback {

    private static final int DEFAULT_TRIM_SIZE = 30;

    private List<Drawable> drawables = new ArrayList<>();

    public List<Drawable> getDrawables(){
        return drawables;
    }

    protected void setDrawables(List<Drawable> drawables){
        this.drawables = drawables;
    }

    public int getSize(){
        return drawables.size();
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
