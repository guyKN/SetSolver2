package com.guykn.setsolver.imageprocessing.classify;

import android.graphics.Bitmap;

import com.guykn.setsolver.set.setcardfeatures.SetCardFeature;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.Closeable;

public interface FeatureClassifier<CardFeature extends SetCardFeature<?>> extends Closeable {
    public CardFeature classifyCardFeature(Bitmap bitmap);
    public CardFeature classifyCardFeature(TensorImage inputImageBuffer);
    public TensorImage loadImage(Bitmap bitmap);

    public void close();
}
