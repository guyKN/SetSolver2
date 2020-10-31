package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.set.PositionlessSetCard;

import org.opencv.core.Mat;

import java.io.Closeable;
import java.io.IOException;

public interface CardClassifier extends Closeable {
    public PositionlessSetCard classify(Mat image);

    public interface CardClassifierFactory{
        public CardClassifier createCardClassifier(Context context, ImageProcessingConfig config) throws IOException;
    }
}