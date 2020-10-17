package com.guykn.setsolver.imageprocessing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.detect.CardAction;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Tensor;

import java.nio.FloatBuffer;

import static org.opencv.core.CvType.CV_32F;

public class ImageProcessingManger {
    private CardDetector detector;
    private CardClassifier classifier;
    public ImageProcessingManger(CardDetector detector, CardClassifier classifier){
        this.detector = detector;
        this.classifier = classifier;
    }


    public RotatedRectangleList getCardPositions(){
        return detector.getAllCardRectangles();
    }

    /**
     * Given a bitmap, returns a mutable Bitmap that is otherwise exactly the save
     * @param src A bitmap
     * @return A mutable bitmap that's otherwise the same as src.
     */
    public static Bitmap copyBitmapAsMutable(Bitmap src){
        return src.copy(src.getConfig(), true);
    }

    public static Mat nv21ToRgbMat(byte[] data, int width, int height){
        Mat nv21Mat = new Mat(height+height/2, width, CvType.CV_8UC1);
        Mat rgbMat = new Mat(nv21Mat.height(), nv21Mat.width(), CvType.CV_8UC1);
        nv21Mat.put(0,0, data);
        Imgproc.cvtColor(nv21Mat, rgbMat, Imgproc.COLOR_YUV2RGB_NV21);
        return rgbMat;
    }

    //todo: try javacv

    public static Bitmap matToBitmap(Mat mat){
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }


}
