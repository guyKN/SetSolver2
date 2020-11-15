package com.guykn.setsolver.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class DrawingHelper {
    private DrawingHelper(){}

    static int[] setDisplayColors = new int[] {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFF00FFFF,
            0xFF880000, 0xFF008800, 0xFF000088, 0xFF888800, 0xFF008888};

    public static final int maxPossibleNumSets = 10;
    public static Paint[] setDisplayPaints;


    static {
        setDisplayPaints = new Paint[maxPossibleNumSets];
        for(int i = 0; i< maxPossibleNumSets; i++){
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStrokeWidth(25f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(setDisplayColors[i]);
            setDisplayPaints[i] = paint;
        }
    }

    public static Paint getPaintForId(int id){
        return setDisplayPaints[id];
    }

    public static void drawTextOnCanvasWithLineBreaks(Canvas canvas,
                                               String text,
                                               float x,
                                               float y,
                                               float lineSpacing,
                                               Paint paint){
        float lineIncrement = paint.getTextSize()*lineSpacing;
        String[] textSplit = text.split("\n");
        for(String line : textSplit){
            canvas.drawText(line, x, y, paint);
            y+=lineIncrement;
        }
    }

    public static Rect growRect(Rect originalRect, int growthSize){
        return new Rect(
                originalRect.left - growthSize,
                originalRect.top - growthSize,
                originalRect.right + growthSize,
                 originalRect.bottom + growthSize
        );
    }
}
