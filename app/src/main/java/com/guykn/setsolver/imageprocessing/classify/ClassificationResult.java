package com.guykn.setsolver.imageprocessing.classify;

import com.guykn.setsolver.imageprocessing.classify.depracated.OldFeatureClassifier;

import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class ClassificationResult {
    private final int resultID;
    private final double resultProbability;
    public int getResultID() {
        return resultID;
    }

    public double getResultProbability() {
        return resultProbability;
    }


    private ClassificationResult(int resultID, double resultProbability){
        this.resultID = resultID;
        this.resultProbability = resultProbability;
    }

    //finds te highest probability in a tensorBuffer, and creates a new Classification resault showing its ID and the probability of being right
    public static ClassificationResult fromProbabilityBuffer(TensorBuffer probabilityBuffer){
        double currentMaxProbability =0;
        int currentMaxIndex=0;
        for(int i = 0; i< OldFeatureClassifier.NUM_CATEGORIES; i++){
            double probability = probabilityBuffer.getFloatValue(i);
            if(probability > currentMaxProbability){
                currentMaxProbability = probability;
                currentMaxIndex = i;
            }
        }
        return new ClassificationResult(currentMaxIndex, currentMaxProbability);
    }
}
