package com.guykn.setsolver.imageprocessing.classify.models;

import android.content.Context;
import android.util.Log;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;
import com.guykn.setsolver.imageprocessing.classify.FeatureClassifier;
import com.guykn.setsolver.imageprocessing.classify.MLCardClassifier;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;

public class ColorClassifier extends FeatureClassifier {

    private static final String MODEL_PATH = "Models/New/Color/ColorModel.tflite";

    public ColorClassifier(Context context, ImageProcessingConfig config) throws IOException {
        super(context, MODEL_PATH, config);
    }

    public SetCardColor classifyCard(TensorImage image){
        ClassificationResult res = classify(image);
        Log.d(MLCardClassifier.TAG, "id: " + res.getResultID());
        return new SetCardColor(res);
    }
}
