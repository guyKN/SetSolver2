package com.guykn.setsolver.imageprocessing.detect;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.imageprocessing.Config;
import com.guykn.setsolver.ui.main.CameraFragment;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ContourBasedCardDetector implements CardDetector{
    //todo: manage memory: make sure only things that are necessary are loaded into memory.
    //todo: replace needToDoX booleans with having or not having null as a value
    //todo: remove @Deprecated functions and variables
    //todo: use ML to check that cards are actually cards

    private Mat initialMat;
    private Mat blurredMat = new Mat(); //same as initialMat, but in greyscale and with a gausian filter
    private Mat hierarchy = new Mat();
    private Mat cannyOutput = new Mat();

    List<MatOfPoint> contours = new ArrayList<>();


    private boolean needToDoGaussianFilter;
    private boolean needToDoCanny;
    private boolean needToFindContours;
    private Config config;
    private Random rng = new Random(12789);
    private Context context;

    public enum BackgroundType {BLACK, ORIGINAL_IMAGE, EDGES}

    public ContourBasedCardDetector(Mat mat, Config config, Context context){
        this.config = config;
        this.context = context;

        initialMat = new Mat();
        Size scaledDownSize = new Size(config.image.width,config.image.height);
        Log.d(CameraFragment.TAG, "h: " + scaledDownSize.height + " w: " + scaledDownSize.width);
        Imgproc.resize(mat, initialMat, scaledDownSize, 0,0, Imgproc.INTER_AREA);

        needToDoGaussianFilter = true;
        needToDoCanny = true;
        needToFindContours = true;
    }


    /*Gausinan Filter************************************************************************/
    private void doGaussianFilterIfNecessary(){
        if(needToDoGaussianFilter){
            Imgproc.cvtColor(initialMat, blurredMat, Imgproc.COLOR_BGR2GRAY); //convert to greyscale
            Imgproc.blur(blurredMat, blurredMat, new Size(config.gaussianBlur.radius, config.gaussianBlur.radius));//users gausian blur to remove noise.
            initialMat.release(); //removes the values of initialMal from memory, so that it doesn't waste memory.
            needToDoGaussianFilter = false;
        }
    }
    public Mat getBlurredMat(){
        doGaussianFilterIfNecessary();
        return blurredMat;
    }


    /*Canny Edge Detection************************************************************************/
    private void doCannyEdgeDetectionIfNecessary(){
        if(needToDoCanny){
            doGaussianFilterIfNecessary();
            Imgproc.Canny(blurredMat, cannyOutput, config.cannyEdgeDetection.threshold, config.cannyEdgeDetection.ratio*config.cannyEdgeDetection.threshold);
            blurredMat.release(); //removes blurredMat from memory in order to save memory.
            needToDoCanny = false;
        }
    }
    public Mat getCannyOutput(){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        return cannyOutput;
    }



    /*contours************************************************************************/
    private void findContoursIfNecessary(){
        if(needToFindContours){
            doCannyEdgeDetectionIfNecessary();
            contours = new ArrayList<>();
            Mat reBlurredCanny = new Mat();
            Imgproc.blur(cannyOutput, reBlurredCanny, new Size(config.contours.reBlurRadius, config.contours.reBlurRadius));//re-apply a gausian blur to remove noise in the edges
            Imgproc.findContours(reBlurredCanny, contours, hierarchy, config.contours.hierarchyType, Imgproc.CHAIN_APPROX_SIMPLE);
            needToFindContours = false;
        }
    }

    /**
     * Calls the doAction method on with a setCardPosition for every card found
     * @param cardAction implements CardAction. calls the doAction(GenericRotatedRectangle position) method on this for every card found.
     */
    public void findAllCardsAndDoAction(CardAction cardAction) {
        findContoursIfNecessary();
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
                    GenericRotatedRectangle cardPosition = new GenericRotatedRectangle(rotatedRect, config.image.width, config.image.height);
                    cardAction.doAction(cardPosition);
                }
            }
        }
    }

    /**
     * Returns all cards on the board, as a RotatedRectangleList.
     * Usually not recommended for use if you are trying to also process or classify those rectangles. For that, use findAllCardsAndDo action
     * @return RotatedRectangleList: a list containing all rectangles on the image.
     */
    public RotatedRectangleList getAllCardRectangles(){
        RotatedRectangleList allCardRectangles = new RotatedRectangleList();
        findAllCardsAndDoAction(allCardRectangles);
        return allCardRectangles;
    }








}
