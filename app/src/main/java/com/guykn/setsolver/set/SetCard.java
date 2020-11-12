package com.guykn.setsolver.set;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

public class SetCard extends GenericRotatedRectangle {

    final private PositionlessSetCard card;


    private static final Paint textPaint;

    static {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setStrokeWidth(20f);
        textPaint.setStyle(Paint.Style.FILL);
    }


    //Todo: check if there is a better implementation (Maybe Use a decorator pattern: https://en.wikipedia.org/wiki/Decorator_pattern)
    public SetCard(GenericRotatedRectangle rotatedRect,
                   PositionlessSetCard card) {
        super(rotatedRect);
        this.card = card;
    }


    @Override
    protected void drawOnCanvasRotated(Canvas canvas, Paint paint) {
        super.drawOnCanvasRotated(canvas, paint);
        canvas.drawText(
                card.getDescription(),
                (float) getAdjustedCenterPoint().x,
                (float) getAdjustedCenterPoint().y,
                textPaint
        );
    }

    public SetCardColor getColor() {
        return card.getColor();
    }

    public SetCardCount getCount() {
        return card.getCount();
    }

    public SetCardFill getFill() {
        return card.getFill();
    }

    public SetCardShape getShape() {
        return card.getShape();
    }
}
