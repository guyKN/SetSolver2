package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.set.PositionlessSetCard;

import org.opencv.core.Mat;

import java.io.Closeable;
import java.io.IOException;

public interface CardClassifier extends Closeable {
    //these methods return null if the given card isn't actually a card based on the model
    @Nullable
    public PositionlessSetCard classify(Mat image);
    @Nullable
    public PositionlessSetCard classify(Bitmap bmp);

    public interface CardClassifierFactory{
        public CardClassifier createCardClassifier(Context context, ImageProcessingConfig config) throws IOException;
    }
}