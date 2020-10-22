package com.guykn.setsolver.imageprocessing.detect;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

import org.opencv.core.Mat;

public interface CardDetector {
    public void setConfig(ImageProcessingConfig config);
    public void setMat(Mat mat);
    public void releaseMat();
    public void findAllCardsAndDoAction(CardAction cardAction);
    public RotatedRectangleList getAllCardRectangles();

    interface CardAction {
        public void doAction(GenericRotatedRectangle position);
    }
}