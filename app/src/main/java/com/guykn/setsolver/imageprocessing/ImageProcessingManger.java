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
//todo: try using javaCv instead of opencv
//todo: add static class that converts from types
public class ImageProcessingManger {
    private final CardDetector detector;
    private final CardClassifier classifier;
    public ImageProcessingManger(CardDetector detector, CardClassifier classifier){
        this.detector = detector;
        this.classifier = classifier;
    }

    public RotatedRectangleList getCardPositions(){
        return detector.getAllCardRectangles();
    }


}
