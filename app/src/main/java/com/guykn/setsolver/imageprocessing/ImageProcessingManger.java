package com.guykn.setsolver.imageprocessing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.detect.CardAction;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;

public class ImageProcessingManger {
    private CardDetector detector;
    private CardClassifier classifier;
    public ImageProcessingManger(CardDetector detector, CardClassifier classifier){
        this.detector = detector;
        this.classifier = classifier;
    }


    public RotatedRectangleList getCardPositions(Bitmap originalImage){
        return detector.getAllCardRectangles();
    }

    public static Bitmap byteArrayToBitmap(byte[] imageByteArray){
        return BitmapFactory.decodeByteArray(imageByteArray , 0, imageByteArray.length);
    }

    /**
     * Given a bitmap, returns a mutable Bitmap that is otherwise exactly the save
     * @param src A bitmap
     * @return A mutable bitmap that's otherwise the same as src.
     */
    public static Bitmap copyBitmapAsMutable(Bitmap src){
        return src.copy(src.getConfig(), true);
    }


}
