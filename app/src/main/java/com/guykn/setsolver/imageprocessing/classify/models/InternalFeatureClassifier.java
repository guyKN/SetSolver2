package com.guykn.setsolver.imageprocessing.classify.models;

import android.content.Context;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.FeatureClassifier;
import com.guykn.setsolver.set.setcardfeatures.SetCardFeature;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;

public abstract class InternalFeatureClassifier<CardFeature extends SetCardFeature<?>>
        extends FeatureClassifier {

    public InternalFeatureClassifier(Context context, ImageProcessingConfig config)
            throws IOException {
        super(context, config);
    }

    public abstract CardFeature classifyCardFeature(TensorImage tImage);
}
