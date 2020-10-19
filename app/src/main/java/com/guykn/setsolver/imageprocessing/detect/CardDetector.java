package com.guykn.setsolver.imageprocessing.detect;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.drawing._GenericRotatedRectangle;
import com.guykn.setsolver.imageprocessing.Config;

import org.opencv.core.Mat;

public interface CardDetector {
    public void findAllCardsAndDoAction(Mat image, Config config , CardAction cardAction);

    public RotatedRectangleList getAllCardRectangles(Mat image, Config config);

    interface CardAction {
        public void doAction(GenericRotatedRectangle position);
    }
}