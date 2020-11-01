package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.models.InternalFeatureClassifier;
import com.guykn.setsolver.imageprocessing.image.MatImage;
import com.guykn.setsolver.set.PositionlessSetCard;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;

import org.opencv.core.Mat;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;

public abstract class MLCardClassifier implements CardClassifier {



    public static final String TAG = "MLCardClassifierTAG";

    private final ImageProcessingConfig config;

    private final InternalFeatureClassifier<SetCardColor> colorClassifier;
    private final ImageProcessor preProcessor;
    private final TensorImage tImage;

    public MLCardClassifier(Context context, ImageProcessingConfig config) throws IOException {
        this.config = config;
        colorClassifier = getColorClassifier(context, config);

        preProcessor = getPreProcessor();
        tImage = new TensorImage(colorClassifier.getDataType());
    }





    @Override
    public PositionlessSetCard classify(Mat image) {
        Bitmap bmp = new MatImage(image)
                .toBitmap();
        return classify(bmp);
    }

    public PositionlessSetCard classify(Bitmap bmp){
        tImage.load(bmp);
        preProcessor.process(tImage);

        SetCardColor color = colorClassifier.classifyCardFeature(tImage);
        return new PositionlessSetCard(color, null, null, null);
    }

    @Override
    public void close() throws IOException {
        colorClassifier.close();
        //todo: is this all?
    }



    protected abstract InternalFeatureClassifier<SetCardColor> getColorClassifier(Context context,
                                                                                  ImageProcessingConfig config) throws IOException;

    protected ImageProcessor getPreProcessor(){
        return new ImageProcessor.Builder()
                .add(getResizeOp())
                .add(getNormalizeOp())
                .build();
    }

    protected abstract ResizeOp getResizeOp();
    protected abstract NormalizeOp getNormalizeOp();

/*
    protected ResizeOp getResizeOp() {
        return new ResizeOp(MLCardClassifier.SCALE_DOWN_HEIGHT, MLCardClassifier.SCALE_DOWN_WIDTH, ResizeOp.ResizeMethod.BILINEAR);
    }
*/

/*
    protected NormalizeOp getPreProcessingNormalization(){
        return new NormalizeOp(MLCardClassifier.IMAGE_MEAN, MLCardClassifier.IMAGE_STD);
    }
*/


}
