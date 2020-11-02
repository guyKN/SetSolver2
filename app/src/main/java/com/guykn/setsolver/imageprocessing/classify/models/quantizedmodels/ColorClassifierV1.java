package com.guykn.setsolver.imageprocessing.classify.models.quantizedmodels;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;
import com.guykn.setsolver.imageprocessing.classify.MLCardClassifier;
import com.guykn.setsolver.imageprocessing.classify.models.InternalFeatureClassifier;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;

import java.io.IOException;

public class ColorClassifierV1 extends InternalFeatureClassifier<SetCardColor> {

//    private static final String MODEL_PATH = "Models/New/float/Color/ColorModel.tflite";
    private static final int NUM_CATEGORIES = 3;
    private static final String MODEL_PATH = "Models/New/quantized/Color/model.tflite";

    public ColorClassifierV1(Context context, ImageProcessingConfig config) throws IOException {
        super(context, config);
    }

    @Override
    public SetCardColor classifyCardFeature(Bitmap bitmap){
        ClassificationResult res = classify(bitmap);
        Log.d(MLCardClassifier.TAG, "id: " + res.id);
        return new SetCardColor(res);
    }

    @Override
    protected ModelType getModelType() {
        return ModelType.QUANTIZED;
    }

    @Override
    protected String getModelPath() {
        return MODEL_PATH;
    }

}
