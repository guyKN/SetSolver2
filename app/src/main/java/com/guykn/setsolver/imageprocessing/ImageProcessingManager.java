package com.guykn.setsolver.imageprocessing;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.imageprocessing.detect.ContourBasedCardDetector;
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
    private RotatedRectangleList cardPositions;

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

    public void findCards(){
        detector.setMat(processedMat);
        cardPositions = detector.getAllCardRectangles();
    }
    public RotatedRectangleList getCardPositions(){
        if(cardPositions == null){
            findCards();
        }
        return cardPositions;
    }

    public void saveCardImagesToGallery(ImageFileManager fileManager){
        Mat mat;
        if(config.memoryManagement.shouldReleaseUnprocessedImage){
            mat = processedMat;
        }else{
            mat = unProcessedMat;
        }
        cardPositions.saveToGallery(fileManager, mat);
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
        cardPositions = null;
    }

    public interface ImagePreProcessor{
        public Mat preProcess(Mat mat, ImageProcessingConfig config);
    }

}
