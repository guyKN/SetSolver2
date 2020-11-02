package com.guykn.setsolver.imageprocessing.classify.models;

import android.content.Context;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.classify.MLCardClassifier;
import com.guykn.setsolver.imageprocessing.classify.models.floatmodels.ColorClassifier;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;

import java.io.IOException;

public class FloatCardClassifierV1 extends MLCardClassifier {

    static final int SCALE_DOWN_WIDTH = 224;
    static final int SCALE_DOWN_HEIGHT = 224;

    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    public FloatCardClassifierV1(Context context, ImageProcessingConfig config) throws IOException {
        super(context, config);
    }

    @Override
    protected InternalFeatureClassifier<SetCardColor> getColorClassifier(Context context,
                                                 ImageProcessingConfig config) throws IOException {
        return new ColorClassifier(context, config);
    }

    public static class FloatCardClassifierV1Factory implements CardClassifierFactory{

        @Override
        public CardClassifier createCardClassifier(Context context, ImageProcessingConfig config)
                throws IOException {
            return new FloatCardClassifierV1(context, config);
        }

    }

}