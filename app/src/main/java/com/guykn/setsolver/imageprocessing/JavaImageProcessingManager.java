package com.guykn.setsolver.imageprocessing;

import android.content.Context;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier.CardClassifierFactory;
import com.guykn.setsolver.imageprocessing.classify.MLCardClassifier;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.imageprocessing.detect.ContourCardDetectorWrapper;
import com.guykn.setsolver.imageprocessing.image.Image;
import com.guykn.setsolver.imageprocessing.verify.direct.AverageColorCardVerifier;
import com.guykn.setsolver.imageprocessing.verify.direct.DirectCardVerifier;
import com.guykn.setsolver.imageprocessing.verify.indirect.DBSCANCardVerifier;
import com.guykn.setsolver.imageprocessing.verify.indirect.IndirectCardVerifier;
import com.guykn.setsolver.set.PositionlessSetCard;
import com.guykn.setsolver.set.SetBoardPosition;
import com.guykn.setsolver.set.SetCard;

import org.opencv.core.Mat;

import java.io.IOException;
import java.util.List;

//todo: reserve mat objects in memory so they're not constantly added and deleted
public class JavaImageProcessingManager implements ImageProcessingManager {
    private final CardDetector detector;
    private final CardClassifierFactory classifierFactory;
    private final ImageProcessingManager.ImagePreProcessor preProcessor;

    private Mat unProcessedMat;
    private Mat processedMat;

    private IndirectCardVerifier indirectCardVerifier;
    private DirectCardVerifier directCardVerifier;
    private ImageProcessingConfig config;
    private RotatedRectangleList cardPositions;

    private SetBoardPosition boardPosition;

    private Context context;

    public static final String TAG = "ImageProcessing";

    //todo: add builder method

    public JavaImageProcessingManager(Context context,
                                      CardDetector detector,
                                      CardClassifierFactory classifierFactory,
                                      ImagePreProcessor preProcessor,
                                      IndirectCardVerifier indirectCardVerifier,
                                      DirectCardVerifier directCardVerifier,
                                      ImageProcessingConfig config){
        this.detector = detector;
        this.classifierFactory = classifierFactory;
        this.preProcessor = preProcessor;
        this.indirectCardVerifier = indirectCardVerifier;
        this.directCardVerifier = directCardVerifier;
        this.config = config;

        this.context = context;
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


    public static JavaImageProcessingManager getDefaultManager(Context context, ImageProcessingConfig config){
        CardDetector detector = new ContourCardDetectorWrapper(config);
        CardClassifierFactory classifier = new MLCardClassifier.MLCardClassifierFactory();
        ImageProcessingManager.ImagePreProcessor preProcessor = new StandardImagePreprocessor();
        IndirectCardVerifier indirectCardVerifier = new DBSCANCardVerifier();
        DirectCardVerifier directCardVerifier = new AverageColorCardVerifier();

        return new JavaImageProcessingManager(context, detector,classifier,
                preProcessor, indirectCardVerifier, directCardVerifier,config);
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
                Mat cropped = card.cropToRect(initialMat, config.image.getScaledDownSize());
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

    private void doClassification(){
        if(cardPositions == null){
            findCards();
        }
        Mat mat = getMat();
        boardPosition = new SetBoardPosition();
        try(CardClassifier classifier = classifierFactory.createCardClassifier(context, config)){
            for(GenericRotatedRectangle rect: cardPositions.getDrawables()){
                Mat cropped = rect.cropToRect(mat);

                PositionlessSetCard result = classifier.classify(cropped);
                SetCard card = new SetCard(rect, result);
                boardPosition.addCard(card);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SetBoardPosition getBoard(){
        if(boardPosition == null){
            doClassification();
        }
        return boardPosition;
    }

    public void saveCardImagesToGallery(ImageFileManager fileManager){
        if(cardPositions == null){
            findCards();
        }

        Mat mat = getMat();
        cardPositions.saveToGallery(fileManager, mat, config.image.getScaledDownSize());
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
        boardPosition = null;
    }
}
