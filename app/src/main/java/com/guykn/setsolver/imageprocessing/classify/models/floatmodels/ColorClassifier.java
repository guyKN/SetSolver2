package com.guykn.setsolver.imageprocessing.classify.models.floatmodels;

import android.content.Context;
import android.util.Log;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;
import com.guykn.setsolver.imageprocessing.classify.MLCardClassifier;
import com.guykn.setsolver.imageprocessing.classify.models.InternalFeatureClassifier;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;

public class ColorClassifier extends InternalFeatureClassifier<SetCardColor> {

    private static final String MODEL_PATH = "Models/New/float/Color/ColorModel.tflite";
    private static final int NUM_CATEGORIES = 3;
//    private static final String MODEL_PATH = "Models/New/quantized/Color/model.tflite";

    public ColorClassifier(Context context, ImageProcessingConfig config) throws IOException {
        super(context, config);
    }

    @Override
    public SetCardColor classifyCardFeature(TensorImage image){
        ClassificationResult res = classify(image);
        Log.d(MLCardClassifier.TAG, "id: " + res.getResultID());
        return new SetCardColor(res);
    }

    @Override
    protected int getNumCategories() {
        return NUM_CATEGORIES;
    }

    @Override
    protected String getModelPath() {
        return MODEL_PATH;
    }

}
