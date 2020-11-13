package com.guykn.setsolver.imageprocessing.classify.models.v2floatmodels;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;
import com.guykn.setsolver.imageprocessing.classify.IsACardClassifier;
import com.guykn.setsolver.imageprocessing.classify.tflite.copypasta.Classifier;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;

public class IsACardClassifierV2 extends Classifier
        implements IsACardClassifier {

    private static final String MODEL_PATH = "Models/NewFloat/IsACard/model_unquant.tflite";

    private final ImageProcessingConfig config;

    public IsACardClassifierV2(Context context, ImageProcessingConfig config) throws IOException {
        super(context, Device.CPU, 1);
        this.config = config;
    }

    @Override
    public boolean isACard(TensorImage inputImageBuffer) {
        ClassificationResult res = classify(inputImageBuffer);
        // An ID of zero indicates a card, an ID of 1 indicates it's not a card
        return res.id == 0;
    }

    @Override
    public boolean isACard(Bitmap bitmap) {
        ClassificationResult res = classify(bitmap);
        // An ID of zero indicates a card, an ID of 1 indicates it's not a card
        return res.id == 0;
    }


    @Override
    protected String getModelPath() {
        return MODEL_PATH;
    }

    @Override
    protected ModelType getModelType() {
        return ModelType.FLOAT;
    }
}
