package com.guykn.setsolver.set;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.guykn.setsolver.drawing.DrawingHelper;
import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

import java.util.ArrayList;
import java.util.List;

public class SetCard extends GenericRotatedRectangle {

    static final int TEXT_SHIFT_RIGHT = 30;
    static final int TEXT_SHIFT_BOTTOM = 45;
    static final float TEXT_LINE_SPACING = 1f;
    private static final Paint textPaint;

    static {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setStrokeWidth(20f);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(40f);
    }

    final private PositionlessSetCard card;
    private final List<Integer> setsContainingThisCard = new ArrayList<>();


    public SetCard(GenericRotatedRectangle rotatedRect, PositionlessSetCard card) {
        super(rotatedRect);
        this.card = card;
    }

    public boolean isSameAs(SetCard otherCard){
        return this.card.isSameAs(otherCard.card);
    }

    @Override
    protected void drawOnCanvasRotated(Canvas canvas, Paint paint) {
        DrawingHelper.drawTextOnCanvasWithLineBreaks(
                canvas,
                card.getVeryShortDescription(),
                (float) getAdjustedCardRect().left + TEXT_SHIFT_RIGHT,
                (float) getAdjustedCardRect().top + TEXT_SHIFT_BOTTOM,
                TEXT_LINE_SPACING,
                textPaint
        );

        if(setsContainingThisCard.isEmpty()){
            super.drawOnCanvasRotated(canvas, paint);
        }else {
            int currentRectangleGrowth = 0;
            for(int setId: setsContainingThisCard){

            }
        }
    }

    public void addToSet(int setId){
        setsContainingThisCard.add(setId);
    }

    public SetCardShape.SetCardShapeEnum getShapeEnum(){
        return card.shape.shapeEnum;
    }

    public SetCardColor.SetCardColorEnum getColorEnum(){
        return card.color.colorEnum;
    }

    public SetCardCount.SetCardCountEnum getCountEnum(){
        return card.count.countEnum;
    }

    public SetCardFill.SetCardFillEnum getFillEnum(){
        return card.fill.fillEnum;
    }

    public SetCardColor getColor() {
        return card.color;
    }

    public SetCardCount getCount() {
        return card.count;
    }

    public SetCardFill getFill() {
        return card.fill;
    }

    public SetCardShape getShape() {
        return card.shape;
    }
}
