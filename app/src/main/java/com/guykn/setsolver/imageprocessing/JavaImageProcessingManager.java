package com.guykn.setsolver.imageprocessing;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.imageprocessing.detect.ContourCardDetectorWrapper;
import com.guykn.setsolver.imageprocessing.image.Image;
import com.guykn.setsolver.imageprocessing.verify.direct.AverageColorCardVerifier;
import com.guykn.setsolver.imageprocessing.verify.direct.DirectCardVerifier;
import com.guykn.setsolver.imageprocessing.verify.indirect.DBSCANCardVerifier;
import com.guykn.setsolver.imageprocessing.verify.indirect.IndirectCardVerifier;

import org.opencv.core.Mat;

import java.util.List;

//todo: reserve mat objects in memory so they're not constantly added and deleted
//todo: rewrite in C++?
public class JavaImageProcessingManager implements ImageProcessingManager {
    private final CardDetector detector;
    private final CardClassifier classifier;
    private final ImageProcessingManager.ImagePreProcessor preProcessor;

    private Mat unProcessedMat;
    private Mat processedMat;

    private IndirectCardVerifier indirectCardVerifier;
    private DirectCardVerifier directCardVerifier;
    private ImageProcessingConfig config;
    private RotatedRectangleList cardPositions;

    public static final String TAG = "ImageProcessing";

    public JavaImageProcessingManager(CardDetector detector, CardClassifier classifier,
                                      ImageProcessingManager.ImagePreProcessor preProcessor,
                                      IndirectCardVerifier indirectCardVerifier,
                                      DirectCardVerifier directCardVerifier,
                                      ImageProcessingConfig config){
        this.detector = detector;
        this.classifier = classifier;
        this.preProcessor = preProcessor;
        this.indirectCardVerifier = indirectCardVerifier;
        this.directCardVerifier = directCardVerifier;
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
    }


    public static JavaImageProcessingManager getDefaultManager(ImageProcessingConfig config){
        CardDetector detector = new ContourCardDetectorWrapper(config);
        CardClassifier classifier = null;//todo: actually implement
        ImageProcessingManager.ImagePreProcessor preProcessor = new StandardImagePreprocessor();
        IndirectCardVerifier outlierDetector = new DBSCANCardVerifier();
        DirectCardVerifier directCardVerifier = new AverageColorCardVerifier();

        return new JavaImageProcessingManager(detector,classifier,
                preProcessor, outlierDetector, directCardVerifier,config);
    }

    private void findCards(){
        cardPositions = detector.getAllCardRectangles(processedMat);
        if(config.contourVerification.shouldDoOutlierDetection){
            cardPositions = indirectCardVerifier.removeFalsePositives(cardPositions, config);
        }
        if(config.contourVerification.shouldDoAverageColorCheck){

            Mat initialMat = getMat();

            List<GenericRotatedRectangle> cardRects = cardPositions.getDrawables();

            for(int i=0;i<cardRects.size();i++){
                GenericRotatedRectangle card = cardRects.get(i);
                Mat cropped = card.cropToRect(initialMat, config.image.scaledDownSize);
                if(directCardVerifier.isFalsePositive(cropped, config)){
                    // the rectangle was actually a false positive, so remove it
                    cardRects.remove(i);
                    i--; //decrease i in order to not skip over elements
                }
            }
        }
    }
    public RotatedRectangleList getCardPositions(){
        if(cardPositions == null){
            findCards();
        }
        return cardPositions;
    }

    public void saveCardImagesToGallery(ImageFileManager fileManager){
        Mat mat = getMat();
        cardPositions.saveToGallery(fileManager, mat, config.image.scaledDownSize);
    }


    public void saveOriginalImageToGallery(ImageFileManager fileManager){
        Mat mat = getMat();
        fileManager.saveToGallery(mat);
    }



    private Mat getMat() {
        Mat mat;
        if (config.memoryManagement.shouldReleaseUnprocessedImage) {
            mat = processedMat;
        } else {
            mat = unProcessedMat;
        }
        return mat;
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
