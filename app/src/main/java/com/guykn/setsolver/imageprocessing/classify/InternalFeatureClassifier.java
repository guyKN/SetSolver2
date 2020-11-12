package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.tflite.copypasta.Classifier;
import com.guykn.setsolver.set.setcardfeatures.SetCardFeature;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;

public abstract class InternalFeatureClassifier<CardFeature extends SetCardFeature<?>>
        extends Classifier {

    private final ImageProcessingConfig config;

    public InternalFeatureClassifier(Context context, ImageProcessingConfig config)
            throws IOException {
        super(context, Device.CPU, 1); //todo: maybe make this changeable
        this.config = config;
    }

    public abstract CardFeature classifyCardFeature(Bitmap bitmap);
    public abstract CardFeature classifyCardFeature(TensorImage inputImageBuffer);


    protected ImageProcessingConfig getConfig() {
        return config;
    }
}