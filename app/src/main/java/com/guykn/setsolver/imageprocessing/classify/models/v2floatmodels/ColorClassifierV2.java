package com.guykn.setsolver.imageprocessing.classify.models.v2floatmodels;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;
import com.guykn.setsolver.imageprocessing.classify.InternalFeatureClassifier;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;

public class ColorClassifierV2 extends InternalFeatureClassifier<SetCardColor> {

    private static final String MODEL_PATH = "Models/NewFloat/Color/model_unquant.tflite";

    public ColorClassifierV2(Context context, ImageProcessingConfig config) throws IOException {
        super(context, config);
    }

    @Override
    public SetCardColor classifyCardFeature(Bitmap bitmap){
        ClassificationResult res = classify(bitmap);
        return new SetCardColor(res);
    }

    @Override
    public SetCardColor classifyCardFeature(TensorImage inputImageBuffer) {
        ClassificationResult res = classify(inputImageBuffer);
        return new SetCardColor(res);
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
