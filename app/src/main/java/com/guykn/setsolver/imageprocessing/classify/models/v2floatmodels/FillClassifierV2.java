package com.guykn.setsolver.imageprocessing.classify.models.v2floatmodels;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;
import com.guykn.setsolver.imageprocessing.classify.InternalFeatureClassifier;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;

public class FillClassifierV2 extends InternalFeatureClassifier<SetCardFill> {

    private static final String MODEL_PATH = "Models/NewFloat/Fill/model_unquant.tflite";

    public FillClassifierV2(Context context, ImageProcessingConfig config) throws IOException {
        super(context, config);
    }

    @Override
    public SetCardFill classifyCardFeature(Bitmap bitmap){
        ClassificationResult res = classify(bitmap);
        return new SetCardFill(res);
    }

    @Override
    public SetCardFill classifyCardFeature(TensorImage inputImageBuffer) {
        ClassificationResult res = classify(inputImageBuffer);
        return new SetCardFill(res);
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
