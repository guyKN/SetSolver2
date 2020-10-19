package com.guykn.setsolver.imageprocessing.detect;

import android.content.Context;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.drawing._GenericRotatedRectangle;
import com.guykn.setsolver.imageprocessing.Config;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ContourCardDetectorWrapper implements CardDetector{

    private final Context context;
    public ContourCardDetectorWrapper(Context context){
        this.context = context;
    }
    /**
     * Calls the doAction method on with a setCardPosition for every card found in the image
     * @param image the Image to process
     * @param config the config that describes thresholds and other settings
     * @param action implements CardAction. calls the doAction(GenericRotatedRectangle position) method on this for every card found.
     */

    @Override
    public void findAllCardsAndDoAction(Mat image, Config config, CardAction action){
        ContourBasedCardDetector cardDetector =
                new ContourBasedCardDetector(image, config, context);
        cardDetector.findAllCardsAndDoAction(action);
    }

    /**
     * Returns all cards on the board, as a RotatedRectangleList.
     * Usually not recommended for use if you are trying to also process or classify those rectangles. For that, use findAllCardsAndDo action
     * @return RotatedRectangleList: a list containing all rectangles on the image.
     */
    @Override
    public RotatedRectangleList getAllCardRectangles(Mat image, Config config){
        RotatedRectangleList allCardRectangles = new RotatedRectangleList();
        findAllCardsAndDoAction(image, config, allCardRectangles);
        return allCardRectangles;
    }


    private class ContourBasedCardDetector{
        //todo: manage memory: make sure only things that are necessary are loaded into memory.
        //todo: replace needToDoX booleans with having or not having null as a value
        //todo: remove @Deprecated functions and variables
        //todo: use ML to check that cards are actually cards

        private final Mat initialMat;
        private final Mat blurredMat = new Mat(); //same as initialMat, but in greyscale and with a gausian filter
        private final Mat hierarchy = new Mat();
        private final Mat cannyOutput = new Mat();

        List<MatOfPoint> contours = new ArrayList<>();


        private final Config config;
        private final Context context;

        public ContourBasedCardDetector(Mat initialMat, Config config, Context context){
            this.config = config;
            this.context = context;
            this.initialMat = initialMat;
        }


        private void doGaussianFilter(){
            Imgproc.cvtColor(initialMat, blurredMat, Imgproc.COLOR_BGR2GRAY); //convert to greyscale
            Imgproc.blur(blurredMat, blurredMat, new Size(
                    config.gaussianBlur.radius, config.gaussianBlur.radius));

        }

        private void doCannEdgeDetection(){
            doGaussianFilter();
            Imgproc.Canny(blurredMat, cannyOutput, config.cannyEdgeDetection.threshold, config.cannyEdgeDetection.ratio*config.cannyEdgeDetection.threshold);
            blurredMat.release(); //removes blurredMat from memory in order to save memory.
        }

        private void findContours(){
            doCannEdgeDetection();
            contours = new ArrayList<>();
            Mat reBlurredCanny = new Mat();
            Imgproc.blur(cannyOutput, reBlurredCanny, new Size(config.contours.reBlurRadius, config.contours.reBlurRadius));//re-apply a gausian blur to remove noise in the edges
            cannyOutput.release();
            Imgproc.findContours(reBlurredCanny, contours, hierarchy, config.contours.hierarchyType, Imgproc.CHAIN_APPROX_SIMPLE);
            reBlurredCanny.release();
        }

        public void findAllCardsAndDoAction(CardAction cardAction) {
            findContours();
            for (MatOfPoint contour : contours) {
                MatOfPoint2f f_contour = new MatOfPoint2f();
                contour.convertTo(f_contour, CvType.CV_32FC2); //convert to matofpoint2f
                double arcLength = Imgproc.arcLength(f_contour, true);
                if (arcLength > config.contours.minContourPerimeter) {
                    if(config.contours.useEpsilon) {
                        double epsilon = arcLength * config.contours.epsilonMultiplier;
                        Imgproc.approxPolyDP(f_contour, f_contour, epsilon, true);
                    }
                    RotatedRect rotatedRect = Imgproc.minAreaRect(f_contour);
                    double rectArea = rotatedRect.size.area();
                    System.out.println(rectArea);
                    if(rectArea > config.contours.minContourArea) {
                        GenericRotatedRectangle cardPosition = new GenericRotatedRectangle(
<<<<<<< HEAD
                                rotatedRect, initialMat.size());
=======
                                rotatedRect, config.image.width, config.image.height);
>>>>>>> parent of 6c8959f... Scale down images with a consistent aspect ratio
                        cardAction.doAction(cardPosition);
                    }
                }
            }
        }
    }
}
