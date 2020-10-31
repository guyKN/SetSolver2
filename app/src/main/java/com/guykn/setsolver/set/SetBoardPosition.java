package com.guykn.setsolver.set;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.guykn.setsolver.drawing.DrawableList;

//todo: implement logic to find sets
public class SetBoardPosition extends DrawableList<SetCard> {
    public void addCard(SetCard card){
        addDrawable(card);
    }



    @Override
    public void drawOnCanvas(Canvas canvas, Paint paint) {
        for(SetCard card: getDrawables()){


            Paint modifiedColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            modifiedColorPaint.setColor(card.getColor().getColor().getColorCode());
            modifiedColorPaint.setStrokeWidth(10f);
            modifiedColorPaint.setStyle(Paint.Style.STROKE);
            card.drawOnCanvas(canvas, modifiedColorPaint);
        }
    }



}
