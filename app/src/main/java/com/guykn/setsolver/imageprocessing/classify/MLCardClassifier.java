package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.image.MatImage;
import com.guykn.setsolver.set.PositionlessSetCard;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

import org.opencv.core.Mat;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;

public abstract class MLCardClassifier implements CardClassifier {



    public static final String TAG = "MLCardClassifierTAG";

    private final ImageProcessingConfig config;

    private final FeatureClassifier<SetCardColor> colorClassifier;
    private final FeatureClassifier<SetCardCount> countClassifier;
    private final FeatureClassifier<SetCardShape> shapeClassifier;
    private final FeatureClassifier<SetCardFill> fillClassifier;

    public MLCardClassifier(Context context, ImageProcessingConfig config) throws IOException {
        this.config = config;
        colorClassifier = createColorClassifier(context, config);
        countClassifier = createCountClassifier(context, config);
        shapeClassifier = createShapeClassifier(context, config);
        fillClassifier = createFillClassifier(context, config);
    }

    @Override
    public PositionlessSetCard classify(Mat image) {
        Bitmap bmp = new MatImage(image).toBitmap();
        return classify(bmp);
    }

    public PositionlessSetCard classify(Bitmap bmp){
        TensorImage inputImageBuffer = colorClassifier.loadImage(bmp);
        SetCardColor color = colorClassifier.classifyCardFeature(inputImageBuffer);
        SetCardCount count = countClassifier.classifyCardFeature(inputImageBuffer);
        SetCardShape shape = shapeClassifier.classifyCardFeature(inputImageBuffer);
        SetCardFill fill = fillClassifier.classifyCardFeature(inputImageBuffer);
        return new PositionlessSetCard(color, count, fill, shape);
    }

    @Override
    public void close() {
        //todo: is this all?
        if(colorClassifier != null) {
            colorClassifier.close();
        }
        if(shapeClassifier != null){
            shapeClassifier.close();
        }
        if(countClassifier != null){
            countClassifier.close();
        }
        if(fillClassifier != null){
            fillClassifier.close();
        }
    }

    protected abstract FeatureClassifier<SetCardCount>
            createCountClassifier(Context context, ImageProcessingConfig config) throws IOException;


    protected abstract FeatureClassifier<SetCardColor>
            createColorClassifier(Context context, ImageProcessingConfig config) throws IOException;

    protected abstract FeatureClassifier<SetCardShape>
            createShapeClassifier(Context context, ImageProcessingConfig config) throws IOException;

    protected abstract FeatureClassifier<SetCardFill>
    createFillClassifier(Context context, ImageProcessingConfig config) throws IOException;
}
