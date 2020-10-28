package com.guykn.setsolver.imageprocessing.detect.outlierdetection;

import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

public interface CardVerifier {
    public RotatedRectangleList removeFalsePositives(RotatedRectangleList rectangleList);
    public void setConfig(ImageProcessingConfig config);

}
