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
    private final MatPreProcessor preProcessor;

    public ImageProcessingManager(CardDetector detector,
                                  CardClassifier classifier, MatPreProcessor preProcessor){
        this.detector = detector;
        this.classifier = classifier;
        this.preProcessor = preProcessor;
    }

    public static ImageProcessingManager getDefaultManager(Context context){
        CardDetector detector = new ContourCardDetectorWrapper(context);
        CardClassifier classifier = null;//todo: actually implement
        MatPreProcessor preProcessor =
                (Mat src, Config config) ->
                        ImageTypeConverter.scaleDown(src, config.image.totalPixels);

        return new ImageProcessingManager(detector,classifier, preProcessor);
    }

    public RotatedRectangleList getCardPositions(Mat image, Config config){
        Mat scaledDown = preProcessor.preProcess(image, config);
        image.release();
        return detector.getAllCardRectangles(scaledDown, config);
    }

    public void close() {
        //todo: implement method
    }

    interface MatPreProcessor{
        public Mat preProcess(Mat src, Config config);
    }
}
