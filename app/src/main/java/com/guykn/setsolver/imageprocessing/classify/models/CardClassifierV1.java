package com.guykn.setsolver.imageprocessing.classify.models;

import android.content.Context;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.classify.MLCardClassifier;
import com.guykn.setsolver.imageprocessing.classify.models.quantizedmodels.ColorClassifierV1;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;

import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;

public class CardClassifierV1 extends MLCardClassifier {

    static final int SCALE_DOWN_WIDTH = 224;
    static final int SCALE_DOWN_HEIGHT = 224;

    private static final float IMAGE_MEAN = 0f;
    private static final float IMAGE_STD = 1f;

    public CardClassifierV1(Context context, ImageProcessingConfig config) throws IOException {
        super(context, config);
    }

    @Override
    protected InternalFeatureClassifier<SetCardColor> getColorClassifier(Context context,
                                                                         ImageProcessingConfig config) throws IOException {
        return new ColorClassifierV1(context, config);
    }

    @Override
    protected ResizeOp getResizeOp() {
        return new ResizeOp(SCALE_DOWN_HEIGHT, SCALE_DOWN_WIDTH, ResizeOp.ResizeMethod.BILINEAR);
    }

    @Override
    protected NormalizeOp getNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

    public static class CardClassifierV1Factory implements CardClassifierFactory{

        @Override
        public CardClassifier createCardClassifier(Context context, ImageProcessingConfig config)
                throws IOException {
            return new CardClassifierV1(context, config);
        }

    }

}