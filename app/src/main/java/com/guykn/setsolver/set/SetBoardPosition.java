package com.guykn.setsolver.set;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.guykn.setsolver.drawing.DrawableList;

//todo: implement logic to find sets
public class SetBoardPosition extends DrawableList<SetCard> {
    public void addCard(SetCard card) {
        addDrawable(card);
    }

    public static final Paint redPaint;
    public static final Paint greenPaint;
    public static final Paint purplePaint;

    static {
        redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(10f);
        redPaint.setStyle(Paint.Style.STROKE);
    }

    static {
        greenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStrokeWidth(10f);
        greenPaint.setStyle(Paint.Style.STROKE);
    }

    static {
        purplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        purplePaint.setColor(Color.MAGENTA);
        purplePaint.setStrokeWidth(10f);
        purplePaint.setStyle(Paint.Style.STROKE);
    }


    @Override
    public void drawOnCanvas(Canvas canvas, Paint paint) {
        for (SetCard card : getDrawables()) {
            Paint currentPaint;
            Log.d(TAG, "Color: " + card.getColor().getColor().getName());

            switch (card.getColor().getColor().getName()) {
                case "Green":
                    currentPaint = greenPaint;
                    break;
                case "Red":
                    currentPaint = redPaint;
                    break;
                case "Purple":
                    currentPaint = purplePaint;
                    break;
                default:
                    throw new IllegalStateException("the given color is neither red, green, nor purple");
            }

            Log.d(TAG, currentPaint == redPaint ? "red paint" : "not red paint");



            card.drawOnCanvas(canvas, currentPaint);
        }
    }


}
