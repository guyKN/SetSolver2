package com.guykn.setsolver.imageprocessing;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.imageprocessing.detect.ContourCardDetectorWrapper;
import com.guykn.setsolver.imageprocessing.detect.outlierdetection.DBSCANOutlierDetector;
import com.guykn.setsolver.imageprocessing.detect.outlierdetection.OutlierDetector;
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

    private OutlierDetector outlierDetector;
    private ImageProcessingConfig config;
    private RotatedRectangleList cardPositions;

    public JavaImageProcessingManager(CardDetector detector, CardClassifier classifier,
                                      ImageProcessingManager.ImagePreProcessor preProcessor,
                                      OutlierDetector outlierDetector, ImageProcessingConfig config){
        this.detector = detector;
        this.classifier = classifier;
        this.preProcessor = preProcessor;
        this.outlierDetector = outlierDetector;
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
    }


    public static JavaImageProcessingManager getDefaultManager(ImageProcessingConfig config){
        CardDetector detector = new ContourCardDetectorWrapper(config);
        CardClassifier classifier = null;//todo: actually implement
        ImageProcessingManager.ImagePreProcessor preProcessor = new StandardImagePreprocessor();
        OutlierDetector outlierDetector = new DBSCANOutlierDetector(config);

        return new JavaImageProcessingManager(detector,classifier,
                preProcessor, outlierDetector, config);
    }

    private void findCards(){
        cardPositions = detector.getAllCardRectangles(processedMat);
        if(config.outlierDetection.shouldDoOutlierDetection){
            cardPositions = outlierDetector.removeOutliers(cardPositions);
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
        cardPositions = outlierDetector.removeOutliers(cardPositions);
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
