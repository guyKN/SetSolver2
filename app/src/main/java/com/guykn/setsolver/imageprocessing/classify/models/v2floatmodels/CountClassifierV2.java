package com.guykn.setsolver.imageprocessing.classify.models.v2floatmodels;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;
import com.guykn.setsolver.imageprocessing.classify.InternalFeatureClassifier;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;

public class CountClassifierV2 extends InternalFeatureClassifier<SetCardCount> {

    private static final String MODEL_PATH = "Models/NewFloat/Count/model_unquant.tflite";

    public CountClassifierV2(Context context, ImageProcessingConfig config) throws IOException {
        super(context, config);
    }

    @Override
    public SetCardCount classifyCardFeature(Bitmap bitmap){
        ClassificationResult res = classify(bitmap);
        return new SetCardCount(res);
    }

    @Override
    public SetCardCount classifyCardFeature(TensorImage inputImageBuffer) {
        ClassificationResult res = classify(inputImageBuffer);
        return new SetCardCount(res);
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
