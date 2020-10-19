package com.guykn.setsolver.imageprocessing.classify;

import android.graphics.Bitmap;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.imageprocessing.ImageTypeConverter;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.set.PositionlessSetCard;
import com.guykn.setsolver.set.SetBoardPosition;
import com.guykn.setsolver.set.SetCard;

import org.opencv.core.Mat;

public abstract class CardClassifier implements CardDetector.CardAction {
    protected Mat originalImageMat;
    private SetBoardPosition board;
    @Override
    public void doAction(GenericRotatedRectangle cardRect) {
        SetCard card = classify(cardRect);
        board.addCard(card);
    }

    public SetCard classify(GenericRotatedRectangle cardRect){
        Mat cropped = cardRect.cropToRect(originalImageMat);//crops the bitmap to contain just the rectangle specified
        Bitmap croppedBitmap = ImageTypeConverter.matToBitmap(cropped);
        PositionlessSetCard card = classify(croppedBitmap);
        return new SetCard(cardRect, card);
    }

    public SetBoardPosition getBoard(){
        return board;
    }

    protected abstract PositionlessSetCard classify(Bitmap cropped);
}