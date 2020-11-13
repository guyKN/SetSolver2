package com.guykn.setsolver.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

public class DrawingHelper {
    private DrawingHelper(){}

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
}
