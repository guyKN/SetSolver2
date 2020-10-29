package com.guykn.setsolver.imageprocessing.verify.indirect;

import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

public interface IndirectCardVerifier {
    public RotatedRectangleList removeFalsePositives(RotatedRectangleList rectangleList,
                                                     ImageProcessingConfig config);
}
