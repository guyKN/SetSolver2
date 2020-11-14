package com.guykn.setsolver.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.guykn.setsolver.R;

public class DrawingHelper {
    private DrawingHelper(){}

    int[] setDisplayColors = new int[] {0xFF0000,
            0x00FF00,
    0x0000FF,
    };

    public static final int maxPossibleNumSets = 5;
    public static Paint[] setDisplayPaints;


    static {

        setDisplayPaints = new Paint[maxPossibleNumSets];
        for(int i = 0; i< maxPossibleNumSets; i++){
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStrokeWidth(30f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(R.color.);
            setDisplayPaints[i]
        }
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
                originalRect.left + growthSize,
                originalRect.top + growthSize,
                originalRect.right + growthSize,
                 originalRect.bottom + growthSize
        );
    }
}
