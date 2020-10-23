package com.guykn.setsolver.imageprocessing.detect;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ContourBasedCardDetector implements CardDetector {
    //todo: manage memory: make sure only things that are necessary are loaded into memory.
    //todo: maybe go back to having a wrapper class?
    //todo: use ML to check that cards are actually cards
    //todo: go back to old version, since this is not working

    private Mat initialMat;
    private Mat blurredMat = new Mat(); //same as initialMat, but in greyscale and with a gausian filter
    private Mat hierarchy = new Mat();
    private Mat cannyOutput = new Mat();

    List<MatOfPoint> contours = new ArrayList<>();


    private ImageProcessingConfig config;

    public ContourBasedCardDetector(ImageProcessingConfig config){
        this.config = config;
    }


    private void doGaussianFilter(){
        if(initialMat == null){
            throw new IllegalStateException("InitialMat must be asigned before it is used");
        }
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

    @Override
    public void findAllCardsAndDoAction(CardDetector.CardAction cardAction) {
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
                            rotatedRect, initialMat.width(), initialMat.height());
                    cardAction.doAction(cardPosition);
                }
            }
        }
    }

    @Override
    public RotatedRectangleList getAllCardRectangles() {
        RotatedRectangleList rectangleList = new RotatedRectangleList();
        findAllCardsAndDoAction(rectangleList);
        return rectangleList;
    }

    @Override
    public void setConfig(ImageProcessingConfig config) {
        this.config = config;
    }

    @Override
    public void setMat(Mat mat) {
        this.initialMat = mat;
        blurredMat = new Mat();
        hierarchy = new Mat();
        cannyOutput = new Mat();
    }

    @Override
    public void releaseMat() {
        if(initialMat != null){
            initialMat.release();
        }
        initialMat = null;
        if(blurredMat != null){
            blurredMat.release();
        }
        blurredMat = null;
        if(hierarchy != null){
            hierarchy.release();
        }
        hierarchy = null;
        if(cannyOutput != null){
            cannyOutput.release();
        }
        cannyOutput = null;
    }
}
