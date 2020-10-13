package com.guykn.setsolver.imageprocessing.detect;

import com.guykn.setsolver.drawing.RotatedRectangleList;

public interface CardDetector {
    public void findAllCardsAndDoAction(CardAction cardAction);

    public RotatedRectangleList getAllCardRectangles();
}