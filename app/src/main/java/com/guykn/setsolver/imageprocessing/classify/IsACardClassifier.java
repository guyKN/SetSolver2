package com.guykn.setsolver.imageprocessing.classify;

import android.graphics.Bitmap;

import org.tensorflow.lite.support.image.TensorImage;

import java.io.Closeable;

public interface IsACardClassifier extends Closeable {
    public boolean isACard(TensorImage inputImageBuffer);
    public boolean isACard(Bitmap bitmap);
    public void close();
}
