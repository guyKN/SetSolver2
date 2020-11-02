package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.set.PositionlessSetCard;

import org.opencv.core.Mat;

import java.io.Closeable;
import java.io.IOException;

public interface CardClassifier extends Closeable {
    public PositionlessSetCard classify(Mat image);
    public PositionlessSetCard classify(Bitmap bmp);

    public interface CardClassifierFactory{
        public CardClassifier createCardClassifier(Context context, ImageProcessingConfig config) throws IOException;
    }
}