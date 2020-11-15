package com.guykn.setsolver.imageprocessing;

import android.content.Context;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier;
import com.guykn.setsolver.imageprocessing.classify.CardClassifier.CardClassifierFactory;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;
import com.guykn.setsolver.imageprocessing.detect.ContourCardDetectorWrapper;
import com.guykn.setsolver.imageprocessing.image.Image;
import com.guykn.setsolver.imageprocessing.verify.indirect.DBSCANCardVerifier;
import com.guykn.setsolver.imageprocessing.verify.indirect.IndirectCardVerifier;
import com.guykn.setsolver.set.PositionlessSetCard;
import com.guykn.setsolver.set.SetBoardPosition;
import com.guykn.setsolver.set.SetCard;

import org.opencv.core.Mat;

import java.io.IOException;

//todo: reserve mat objects in memory so they're not constantly added and deleted
public class JavaImageProcessingManager implements ImageProcessingManager {
    private final CardDetector detector;
    private final CardClassifierFactory classifierFactory;
    private final ImageProcessingManager.ImagePreProcessor preProcessor;

    private Mat unProcessedMat;
    private Mat processedMat;

    private IndirectCardVerifier indirectCardVerifier;
    private final ImageProcessingConfig config;
    private RotatedRectangleList cardPositions;

    private SetBoardPosition boardPosition;

    private Context context;


    public static final String TAG = "ImageProcessing";
    private CardClassifier cardClassifier;

    private JavaImageProcessingManager(Builder builder,
                                       ImageProcessingConfig config) {
        this.detector = builder.detector;
        this.detector.setConfig(config);
        this.classifierFactory = builder.classifierFactory;
        this.preProcessor = builder.preProcessor;
        this.indirectCardVerifier = builder.indirectCardVerifier;
        this.config = config;
        this.context = builder.context;

        if(this.classifierFactory != null){
            try {
                cardClassifier = classifierFactory.createCardClassifier(context, config);
            }catch (IOException e){
                e.printStackTrace();
                //todo: better error Handling
            }
        }
    }

    @Override
    public void setImage(Image image) {
        this.unProcessedMat = image.toMat();
        this.processedMat = preProcessor.preProcess(unProcessedMat, config);
        if (config.memoryManagement.shouldReleaseUnprocessedImage) {
            unProcessedMat.release();
            unProcessedMat = null;
        }
    }

    private void findCards() {
        cardPositions = detector.getAllCardRectangles(processedMat);

        if (config.contourVerification.shouldDoOutlierDetection) {
            cardPositions = indirectCardVerifier.removeFalsePositives(cardPositions, config);
        }

//        if (config.contourVerification.shouldDoAverageColorCheck) {
//
//            Mat initialMat = getMat();
//            List<GenericRotatedRectangle> cardRects = cardPositions.getDrawables();
//            for (int i = 0; i < cardRects.size(); i++) {
//                GenericRotatedRectangle card = cardRects.get(i);
//                Mat cropped = card.cropToRect(initialMat, config.image.getScaledDownSize());
//                if (directCardVerifier.isFalsePositive(cropped, config)) {
//                    // the rectangle was actually a false positive, so remove it
//                    cardRects.remove(i);
//                    i--; //decrease i in order to not skip over elements
//                }
//            }
//        }
    }

    @Override
    public RotatedRectangleList getCardPositions() {
        if (cardPositions == null) {
            findCards();
        }
        return cardPositions;
    }

    private void doClassification() {
        if (cardPositions == null) {
            findCards();
        }
        cardPositions.printStates();
        Mat mat = getMat();
        boardPosition = new SetBoardPosition();
        for (GenericRotatedRectangle rect : cardPositions.getDrawables()) {
            Mat cropped = rect.cropToRect(mat);
            PositionlessSetCard result = cardClassifier.classify(cropped);
            if(result == null){
                //the classifier found that the Image wasn't actually a card
                continue;
            }
            SetCard card = new SetCard(rect, result);
            boardPosition.addCard(card);
        }
        boardPosition.findSets();
    }

    @Override
    public SetBoardPosition getBoard() {
        if (boardPosition == null) {
            doClassification();
        }
        return boardPosition;
    }

    @Override
    public void saveCardImagesToGallery(ImageFileManager fileManager) {
        if (cardPositions == null) {
            findCards();
        }

        Mat mat = getMat();
        cardPositions.saveToGallery(fileManager, mat, config.image.getScaledDownSize());
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


    @Override
    public void finish() {

        if(cardClassifier != null){
            try {
                cardClassifier.close();
            } catch (IOException e) {
                e.printStackTrace();
                //igonred, classifier is probably already closed
            }
        }

        if (processedMat != null) {
            processedMat.release();
        }
        processedMat = null;

        if (unProcessedMat != null) {
            unProcessedMat.release();
        }
        unProcessedMat = null;
        cardPositions = null;
        boardPosition = null;
    }

    public static class Builder
            implements ImageProcessingManagerBuilder {

        private final Context context;
        private CardDetector detector = new ContourCardDetectorWrapper();
        private CardClassifierFactory classifierFactory;
        private ImagePreProcessor preProcessor = new StandardImagePreprocessor();
        private IndirectCardVerifier indirectCardVerifier = new DBSCANCardVerifier();

        public Builder(Context context){
            this.context = context;
        }


        public Builder setDetector(CardDetector detector) {
            this.detector = detector;
            return this;
        }

        public Builder setClassifierFactory(CardClassifierFactory classifierFactory) {
            this.classifierFactory = classifierFactory;
            return this;
        }

        public Builder setPreProcessor(ImagePreProcessor preProcessor) {
            this.preProcessor = preProcessor;
            return this;
        }

        public Builder setIndirectCardVerifier(IndirectCardVerifier indirectCardVerifier) {
            this.indirectCardVerifier = indirectCardVerifier;
            return this;
        }

        public JavaImageProcessingManager build(ImageProcessingConfig config) {
            return new JavaImageProcessingManager(this, config);
        }
    }

}
