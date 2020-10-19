package com.guykn.setsolver.imageprocessing;

import android.content.Context;

import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.imageprocessing.detect.ContourCardDetectorWrapper;

import org.opencv.core.Mat;

//todo: try using javaCv instead of opencv
//todo: add static class that converts from types
public class ImageProcessingManager {
    private final CardDetector detector;
    private final CardClassifier classifier;
    public ImageProcessingManager(CardDetector detector,
                                  CardClassifier classifier){
        this.detector = detector;
        this.classifier = classifier;
    }

    public static ImageProcessingManager getDefaultManager(Context context){
        CardDetector detector = new ContourCardDetectorWrapper(context);
        CardClassifier classifier = null;//todo: actually implement

        return new ImageProcessingManager(detector,classifier);
    }

    public RotatedRectangleList getCardPositions(Mat image, Config config){
        return detector.getAllCardRectangles(image, config);
    }


    public void close() {
        //todo: implement method
    }

}
