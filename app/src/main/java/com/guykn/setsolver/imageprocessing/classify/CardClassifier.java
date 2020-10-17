package com.guykn.setsolver.imageprocessing.classify;

import android.graphics.Bitmap;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.imageprocessing.ImageProcessingManger;
import com.guykn.setsolver.imageprocessing.detect.CardAction;
import com.guykn.setsolver.set.PositionlessSetCard;
import com.guykn.setsolver.set.SetBoard;
import com.guykn.setsolver.set.SetCard;

import org.opencv.core.Mat;

public abstract class CardClassifier implements CardAction {
    protected Mat originalImageMat;
    private SetBoard board;
    @Override
    public void doAction(GenericRotatedRectangle cardRect) {
        SetCard card = classify(cardRect);
        board.addCard(card);
    }

    public SetCard classify(GenericRotatedRectangle cardRect){
        Mat cropped = cardRect.cropToRect(originalImageMat);//crops the bitmap to contain just the rectangle specified
        Bitmap croppedBitmap = ImageProcessingManger.matToBitmap(cropped);
        PositionlessSetCard card = classify(croppedBitmap);
        return new SetCard(cardRect, card);
    }

    public SetBoard getBoard(){ //todo: maybe rename the SetBoard class
        return board;
    }

    protected abstract PositionlessSetCard classify(Bitmap cropped); //todo: figure out a proper type to return here
}