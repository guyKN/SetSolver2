package com.guykn.setsolver.unittest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;
import com.guykn.setsolver.imageprocessing.classify.FeatureClassifier;
import com.guykn.setsolver.imageprocessing.classify.MLCardClassifier;
import com.guykn.setsolver.imageprocessing.classify.models.CardClassifierV1;
import com.guykn.setsolver.imageprocessing.classify.models.floatmodels.ColorClassifier;
import com.guykn.setsolver.set.PositionlessSetCard;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.io.InputStream;

public class ImageClassificationTest {

    static final int SCALE_DOWN_WIDTH = 224;
    static final int SCALE_DOWN_HEIGHT = 224;

    static final float IMAGE_MEAN = 0.0f;
    static final float IMAGE_STD = 1.0f;

    String imRed = "TestImages/SolidRedDiamond2.jpg";
    String imPurple = "TestImages/SolidPurpleDiamond1.jpg";
    String imGreen = "TestImages/SolidGreenSShape2.jpg";


    private final Context context;
    private static final String TAG = MLCardClassifier.TAG;

    public ImageClassificationTest(Context context) {
        this.context = context;
    }

    public void test2() {
        Log.d(TAG, "testing RED: ");
        PositionlessSetCard res = classifyFromFile(imRed);
        Log.d(TAG, "RED result: " + res.getColor().getColor().getName());

        Log.d(TAG, "testing PURPLE: ");
        res = classifyFromFile(imPurple);
        Log.d(TAG, "PURPLE result: " + res.getColor().getColor().getName());

        Log.d(TAG, "testing GREEN: ");
        res = classifyFromFile(imGreen);
        Log.d(TAG, "GREEN result: " + res.getColor().getColor().getName());
    }

    public PositionlessSetCard classifyFromFile(String filepath) {
        try {
            CardClassifierV1 cardClassifier = new CardClassifierV1(context,
                    ImageProcessingConfig.getDefaultConfig());

            Bitmap bmp = loadBitmapAsset(filepath);

            return cardClassifier.classify(bmp);


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


    public void test() {
        try {
            FeatureClassifier colorClassifier = new ColorClassifier(context,
                    ImageProcessingConfig.getDefaultConfig());

            ImageProcessor preProcessor = new ImageProcessor.Builder()
                    .add(getResizeOp())
                    .add(getPreProcessingNormalization())
                    .build();

            Bitmap bmp = loadBitmapAsset(imRed);

            TensorImage tImage = new TensorImage(DataType.UINT8);
            tImage.load(bmp);
            preProcessor.process(tImage);
            ClassificationResult result = colorClassifier.classify(tImage);
            Log.d(TAG, " resultId: " + result.getResultID());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap loadBitmapAsset(String filepath) throws IOException {
        InputStream assetInStream = context.getAssets().open(filepath);
        return BitmapFactory.decodeStream(assetInStream);
    }


    protected ResizeOp getResizeOp() {
        return new ResizeOp(SCALE_DOWN_HEIGHT, SCALE_DOWN_WIDTH, ResizeOp.ResizeMethod.BILINEAR);
    }

    protected NormalizeOp getPreProcessingNormalization() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

}
