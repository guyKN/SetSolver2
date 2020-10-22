package com.guykn.setsolver.imageprocessing;

import android.content.Context;

import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.imageprocessing.detect.ContourBasedCardDetector;
import com.guykn.setsolver.imageprocessing.detect.ContourCardDetectorWrapper;
import com.guykn.setsolver.imageprocessing.image.Image;

import org.opencv.core.Mat;

//todo: see if having this class as a field in CameraPreviewThread is really the best idea.
public class ImageProcessingManager {
    private final CardDetector detector;
    private final CardClassifier classifier;
    private final ImagePreProcessor preProcessor;

    private Mat unProcessedMat;
    private Mat processedMat;
    private ImageProcessingConfig config;

    public ImageProcessingManager(CardDetector detector, CardClassifier classifier,
                                  ImagePreProcessor preProcessor, ImageProcessingConfig config){
        this.detector = detector;
        this.classifier = classifier;
        this.preProcessor = preProcessor;
        this.config = config;
    }

    public void setImage(Image image){
        this.unProcessedMat = image.toMat();
        this.processedMat = preProcessor.preProcess(unProcessedMat, config);
        if(config.memoryManagement.shouldReleaseUnprocessedImage){
            unProcessedMat.release();
            unProcessedMat = null;
        }
    }

    public void setConfig(ImageProcessingConfig config){
        this.config = config;
    }

    public static ImageProcessingManager getDefaultManager(ImageProcessingConfig config){
        CardDetector detector = new ContourBasedCardDetector(config);
        CardClassifier classifier = null;//todo: actually implement
        ImagePreProcessor preProcessor = new StandardImagePreprocessor();

        return new ImageProcessingManager(detector,classifier, preProcessor, config);
    }

    public RotatedRectangleList getCardPositions(){
        detector.setMat(processedMat);
        return detector.getAllCardRectangles();
    }




    public void finish() {
        if(processedMat != null){
            processedMat.release();
        }
        processedMat = null;

        if(unProcessedMat != null){
            unProcessedMat.release();
        }
        unProcessedMat = null;
        detector.releaseMat();
    }

    interface ImagePreProcessor{
        public Mat preProcess(Mat mat, ImageProcessingConfig config);
    }

}
