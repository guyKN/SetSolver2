package com.guykn.setsolver.unittest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.classify.MLCardClassifier;
import com.guykn.setsolver.imageprocessing.classify.models.v2floatmodels.CardClassifierV2;
import com.guykn.setsolver.set.PositionlessSetCard;

import org.tensorflow.lite.support.common.ops.NormalizeOp;
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

    String imRed2 = "TestImages/2_SolidRedCircle1.jpg";
    String imPurple2 = "TestImages/2_SolidPurpleSShape1.jpg";
    String imGreen2 = "TestImages/2_SolidGreenCircle1.jpg";

    String imRed3 = "TestImages/red3.jpg";




    private final Context context;
    private static final String TAG = MLCardClassifier.TAG;

    public ImageClassificationTest(Context context) {
        this.context = context;
    }

    public void test2() {
        Log.d(TAG, "testing RED: ");
        PositionlessSetCard res = classifyFromFile(imRed3);
        Log.d(TAG, "RED result: " + res.color.getName());
        Log.d(TAG, "RED probability: " + res.color.getConfidence());

        Log.d(TAG, "testing PURPLE: ");
        res = classifyFromFile(imPurple2);
        Log.d(TAG, "PURPLE result: " + res.color.getName());
        Log.d(TAG, "PURPLE probability: " + res.color.getConfidence());


        Log.d(TAG, "testing GREEN: ");
        res = classifyFromFile(imGreen2);
        Log.d(TAG, "GREEN result: " + res.color.getName());
        Log.d(TAG, "GREEN probability: " + res.color.getConfidence());
    }

    public PositionlessSetCard classifyFromFile(String filepath) {
        try {
            CardClassifier cardClassifier = new CardClassifierV2(context,
                    ImageProcessingConfig.getDefaultConfig());

            Bitmap bmp = loadBitmapAsset(filepath);

            return cardClassifier.classify(bmp);


        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
