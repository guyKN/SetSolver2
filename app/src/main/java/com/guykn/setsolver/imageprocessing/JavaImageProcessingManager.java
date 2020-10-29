package com.guykn.setsolver.imageprocessing;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.imageprocessing.detect.ContourCardDetectorWrapper;
import com.guykn.setsolver.imageprocessing.detect.cardverification.CardVerifier;
import com.guykn.setsolver.imageprocessing.detect.cardverification.DBSCANCardVerifier;
import com.guykn.setsolver.imageprocessing.image.Image;

import org.opencv.core.Mat;

//todo: reserve mat objects in memory so they're not constantly added and deleted
//todo: rewrite in C++?
public class JavaImageProcessingManager implements ImageProcessingManager {
    private final CardDetector detector;
    private final CardClassifier classifier;
    private final ImageProcessingManager.ImagePreProcessor preProcessor;

    private Mat unProcessedMat;
    private Mat processedMat;

    private CardVerifier cardVerifier;
    private ImageProcessingConfig config;
    private RotatedRectangleList cardPositions;

    public JavaImageProcessingManager(CardDetector detector, CardClassifier classifier,
                                      ImageProcessingManager.ImagePreProcessor preProcessor,
                                      CardVerifier cardVerifier, ImageProcessingConfig config){
        this.detector = detector;
        this.classifier = classifier;
        this.preProcessor = preProcessor;
        this.cardVerifier = cardVerifier;
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
        //todo: change config of everything else too
        this.config = config;
        detector.setConfig(config);
        cardVerifier.setConfig(config);
    }


    public static JavaImageProcessingManager getDefaultManager(ImageProcessingConfig config){
        CardDetector detector = new ContourCardDetectorWrapper(config);
        CardClassifier classifier = null;//todo: actually implement
        ImageProcessingManager.ImagePreProcessor preProcessor = new StandardImagePreprocessor();
        CardVerifier outlierDetector = new DBSCANCardVerifier(config);

        return new JavaImageProcessingManager(detector,classifier,
                preProcessor, outlierDetector, config);
    }

    private void findCards(){
        cardPositions = detector.getAllCardRectangles(processedMat);
        if(config.outlierDetection.shouldDoOutlierDetection){
            cardPositions = cardVerifier.removeFalsePositives(cardPositions);
        }
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

    public void removeOutliers(){
        cardPositions = cardVerifier.removeFalsePositives(cardPositions);
    }

    public void saveOriginalImageToGallery(ImageFileManager fileManager){
        Mat mat;
        if(config.memoryManagement.shouldReleaseUnprocessedImage){
            mat = processedMat;
        }else{
            mat = unProcessedMat;
        }
        fileManager.saveToGallery(mat);
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
        cardPositions = null;
    }

}
