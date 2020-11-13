package com.guykn.setsolver.imageprocessing.classify.models.v2floatmodels;

import android.content.Context;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.classify.FeatureClassifier;
import com.guykn.setsolver.imageprocessing.classify.IsACardClassifier;
import com.guykn.setsolver.imageprocessing.classify.MLCardClassifier;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

import java.io.IOException;

public class CardClassifierV2 extends MLCardClassifier {

    public CardClassifierV2(Context context, ImageProcessingConfig config) throws IOException {
        super(context, config);
    }

    @Override
    protected IsACardClassifier createIsACardClassier(Context context, ImageProcessingConfig config) throws IOException {
        return new IsACardClassifierV2(context, config);
    }

    public static class CardClassifierV2Factory implements CardClassifierFactory{


        @Override
        public CardClassifier createCardClassifier(Context context, ImageProcessingConfig config)
                throws IOException {
            return new CardClassifierV2(context, config);
        }
    }

    @Override
    protected FeatureClassifier<SetCardColor> createColorClassifier(Context context,
                                                ImageProcessingConfig config) throws IOException {
        return new ColorClassifierV2(context, config);
    }

    @Override
    protected FeatureClassifier<SetCardCount> createCountClassifier(
                Context context, ImageProcessingConfig config) throws IOException {
        return new CountClassifierV2(context, config);
    }

    @Override
    protected FeatureClassifier<SetCardShape> createShapeClassifier(
                Context context, ImageProcessingConfig config) throws IOException {
        return new ShapeClassifierV2(context, config);
    }

    @Override
    protected FeatureClassifier<SetCardFill> createFillClassifier(
                Context context, ImageProcessingConfig config) throws IOException {
        return new FillClassifierV2(context, config);
    }
}
