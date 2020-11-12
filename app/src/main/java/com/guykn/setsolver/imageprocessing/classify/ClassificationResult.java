package com.guykn.setsolver.imageprocessing.classify;

import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class ClassificationResult {
    public final int id;
    public final double probability;

    public ClassificationResult(int id, double resultProbability) { //todo: bring back private
        this.id = id;
        this.probability = resultProbability;
    }

    //finds the highest probability in a tensorBuffer, and creates a new Classification resault showing its ID and the probability of being right
    public static ClassificationResult fromProbabilityBuffer(TensorBuffer probabilityBuffer) {
        int numCategories = probabilityBuffer.getFlatSize();
        double currentMaxProbability = 0;
        int currentMaxIndex = 0;
        for (int i = 0; i < numCategories; i++) {
            float probability = probabilityBuffer.getFloatValue(i);
            if (probability > currentMaxProbability) {
                currentMaxProbability = probability;
                currentMaxIndex = i;
            }
        }
        return new ClassificationResult(currentMaxIndex, currentMaxProbability);
    }
}
