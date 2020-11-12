package com.guykn.setsolver.imageprocessing.classify.models.v2floatmodels;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;
import com.guykn.setsolver.imageprocessing.classify.InternalFeatureClassifier;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;

public class ShapeClassifierV2 extends InternalFeatureClassifier<SetCardShape> {

    private static final String MODEL_PATH = "Models/NewFloat/Shape/model_unquant.tflite";

    public ShapeClassifierV2(Context context, ImageProcessingConfig config) throws IOException {
        super(context, config);
    }

    @Override
    public SetCardShape classifyCardFeature(Bitmap bitmap){
        ClassificationResult res = classify(bitmap);
        return new SetCardShape(res);
    }

    @Override
    public SetCardShape classifyCardFeature(TensorImage inputImageBuffer) {
        ClassificationResult res = classify(inputImageBuffer);
        return new SetCardShape(res);
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
