package com.guykn.setsolver.imageprocessing.classify;

import android.graphics.Bitmap;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.imageprocessing.detect.CardAction;
import com.guykn.setsolver.set.PositionlessSetCard;
import com.guykn.setsolver.set.SetBoard;
import com.guykn.setsolver.set.SetCard;

public abstract class CardClassifier implements CardAction {
    protected Bitmap originalImageBitmap;
    private SetBoard board;
    @Override
    public void doAction(GenericRotatedRectangle cardRect) {
        SetCard card = classify(cardRect);
        board.addCard(card);
    }

    public SetCard classify(GenericRotatedRectangle cardRect){
        Bitmap cropped = cardRect.cropToRect(originalImageBitmap);//crops the bitmap to contain just the rectangle specified
        PositionlessSetCard card = classify(cropped);
        return new SetCard(cardRect, card);
    }

    public SetBoard getBoard(){ //todo: maybe rename the SetBoard class
        return board;
    }

    protected abstract PositionlessSetCard classify(Bitmap cropped); //todo: figure out a proper type to return here
}