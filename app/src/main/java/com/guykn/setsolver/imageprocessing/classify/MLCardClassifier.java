package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.models.InternalFeatureClassifier;
import com.guykn.setsolver.imageprocessing.image.MatImage;
import com.guykn.setsolver.set.PositionlessSetCard;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;

import org.opencv.core.Mat;

import java.io.IOException;

public abstract class MLCardClassifier implements CardClassifier {



    public static final String TAG = "MLCardClassifierTAG";

    private final ImageProcessingConfig config;

    private final InternalFeatureClassifier<SetCardColor> colorClassifier;

    public MLCardClassifier(Context context, ImageProcessingConfig config) throws IOException {
        this.config = config;
        colorClassifier = getColorClassifier(context, config);
    }





    @Override
    public PositionlessSetCard classify(Mat image) {
        Bitmap bmp = new MatImage(image).toBitmap();
        return classify(bmp);
    }

    public PositionlessSetCard classify(Bitmap bmp){
        SetCardColor color = colorClassifier.classifyCardFeature(bmp);
        return new PositionlessSetCard(color, null, null, null);
    }

    @Override
    public void close() throws IOException {
        colorClassifier.close();
        //todo: is this all?
    }



    protected abstract InternalFeatureClassifier<SetCardColor> getColorClassifier(Context context,
                                                                                  ImageProcessingConfig config) throws IOException;

}
