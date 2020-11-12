package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.image.MatImage;
import com.guykn.setsolver.set.PositionlessSetCard;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;

import org.opencv.core.Mat;

import java.io.IOException;

public abstract class MLCardClassifier implements CardClassifier {



    public static final String TAG = "MLCardClassifierTAG";

    private final ImageProcessingConfig config;

    private final InternalFeatureClassifier<SetCardColor> colorClassifier;
    private final InternalFeatureClassifier<SetCardCount> countClassifier;

    public MLCardClassifier(Context context, ImageProcessingConfig config) throws IOException {
        this.config = config;
        colorClassifier = getColorClassifier(context, config);
        countClassifier = getCountClassifier(context, config);
    }

    @Override
    public PositionlessSetCard classify(Mat image) {
        Bitmap bmp = new MatImage(image).toBitmap();
        return classify(bmp);
    }

    public PositionlessSetCard classify(Bitmap bmp){
        SetCardColor color = colorClassifier.classifyCardFeature(bmp);
        SetCardCount count = countClassifier.classifyCardFeature(bmp);
        return new PositionlessSetCard(color, count, null, null);
    }

    @Override
    public void close() throws IOException {
        //todo: is this all?
        if(colorClassifier != null) {
            colorClassifier.close();
        }
    }

    protected abstract InternalFeatureClassifier<SetCardCount>
            getCountClassifier(Context context, ImageProcessingConfig config) throws IOException;


    protected abstract InternalFeatureClassifier<SetCardColor>
            getColorClassifier(Context context, ImageProcessingConfig config) throws IOException;

}
