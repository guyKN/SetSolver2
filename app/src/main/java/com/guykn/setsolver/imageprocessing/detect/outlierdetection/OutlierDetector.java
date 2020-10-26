package com.guykn.setsolver.imageprocessing.detect.outlierdetection;

import com.guykn.setsolver.drawing.RotatedRectangleList;

public interface OutlierDetector {
    public RotatedRectangleList removeOutliers(RotatedRectangleList rectangleList);

}
