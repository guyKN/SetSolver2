package com.guykn.setsolver.imageprocessing.verify.direct;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

import org.opencv.core.Mat;

public interface DirectCardVerifier {
    public boolean isFalsePositive(Mat croppedMat, ImageProcessingConfig config);
}
