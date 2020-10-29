package com.guykn.setsolver.imageprocessing.verify.direct;

import android.util.Log;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.JavaImageProcessingManager;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class AverageColorCardVerifier implements DirectCardVerifier {

    @Override
    public boolean isFalsePositive(Mat croppedMat, ImageProcessingConfig config) {
        Scalar meanColorScalar = Core.mean(croppedMat);
        double[] meanColorArray = meanColorScalar.val;
        double averageColor = findAverage(meanColorArray);
        Log.d(JavaImageProcessingManager.TAG, "average Color: " + String.valueOf(averageColor));
        return averageColor < config.contourVerification.averageColorCheckThreshold;
    }

    private static double findAverage(double[] values){
        double total = 0;
        for(double value : values){
            total += value;
        }
        return total/values.length;
    }
}
