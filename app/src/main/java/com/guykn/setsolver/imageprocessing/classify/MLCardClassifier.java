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

    private final InternalFeatureClassifier<SetCardColor> colorClassifier;
    private final InternalFeatureClassifier<SetCardCount> countClassifier;
    private final InternalFeatureClassifier<SetCardShape> shapeClassifier;
    private final InternalFeatureClassifier<SetCardFill> fillClassifier;

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
    }

    protected abstract InternalFeatureClassifier<SetCardCount>
            createCountClassifier(Context context, ImageProcessingConfig config) throws IOException;


    protected abstract InternalFeatureClassifier<SetCardColor>
            createColorClassifier(Context context, ImageProcessingConfig config) throws IOException;

    protected abstract InternalFeatureClassifier<SetCardShape>
            createShapeClassifier(Context context, ImageProcessingConfig config) throws IOException;

    protected abstract InternalFeatureClassifier<SetCardFill>
    createFillClassifier(Context context, ImageProcessingConfig config) throws IOException;
}
