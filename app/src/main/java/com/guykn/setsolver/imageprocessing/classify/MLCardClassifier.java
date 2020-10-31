package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.models.ColorClassifier;
import com.guykn.setsolver.set.PositionlessSetCard;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;

public class MLCardClassifier implements CardClassifier {

    public static final String TAG = "MLCardClassifierTAG";

    static final int SCALE_DOWN_WIDTH = 224;
    static final int SCALE_DOWN_HEIGHT = 224;

    static final float IMAGE_MEAN = 127.5f;
    static final float IMAGE_STD = 127.5f;

    static final float PROBABILITY_MEAN = 0.0f;
    static final float PROBABILITY_STD = 1.0f;
    static final DataType INPUT_DATA_TYPE = DataType.FLOAT32;//todo: is this right

    private final ImageProcessingConfig config;

    private final ColorClassifier colorClassifier;
    private final ImageProcessor preProcessor;
    private final TensorImage tImage;

    public MLCardClassifier(Context context, ImageProcessingConfig config) throws IOException {
        this.config = config;
        colorClassifier = new ColorClassifier(context, config);

        preProcessor = new ImageProcessor.Builder()
                .add(getResizeOp())
                .add(getPreProcessingNormalization())
                .build();

        tImage = new TensorImage(INPUT_DATA_TYPE);
    }

    @Override
    public PositionlessSetCard classify(Mat image) {
        loadTensorImageFromMat(image);
        preProcessor.process(tImage);
        SetCardColor color = colorClassifier.classifyCard(tImage);
        return new PositionlessSetCard(color, null, null, null);
    }

    @Override
    public void close() throws IOException {
        colorClassifier.close();
    }

    protected ResizeOp getResizeOp() {
        return new ResizeOp(MLCardClassifier.SCALE_DOWN_HEIGHT, MLCardClassifier.SCALE_DOWN_WIDTH, ResizeOp.ResizeMethod.BILINEAR);
    }

    protected NormalizeOp getPreProcessingNormalization(){
        return new NormalizeOp(MLCardClassifier.IMAGE_MEAN, MLCardClassifier.IMAGE_STD);
    }


    private void loadTensorImageFromMat(Mat mat){
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        tImage.load(bmp);
        preProcessor.process(tImage);
    }

    public static class MLCardClassifierFactory implements CardClassifierFactory{
        @Override
        public CardClassifier createCardClassifier(Context context, ImageProcessingConfig config) throws IOException{
            return new MLCardClassifier(context, config);
        }
    }
}
