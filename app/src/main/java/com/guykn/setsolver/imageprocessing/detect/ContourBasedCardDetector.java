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

    @Deprecated
    private Mat houghLinesP = new Mat(); //lines after the P hough transform
    @Deprecated
    private Mat houghLines = new Mat();
    @Deprecated
    private List<Integer> indexOfNonSimilarHoughLines;
    @Deprecated
    private List<RotatedRect> allCardRects;
    @Deprecated
    private List<Mat> croppedCards;

    private boolean needToDoGaussianFilter;
    private boolean needToDoCanny;
    private boolean needToFindContours;
    @Deprecated
    private boolean needToDoHughLines_P;
    @Deprecated
    private boolean needToDoHughLines;
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
        needToDoHughLines_P = true;
        needToDoHughLines = true;
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

    /*HoughLines************************************************************************/
    @Deprecated
    private void doHoughLinesIfNecessary(){
        if(needToDoHughLines){
            System.out.println("inside function. RhoJump=" + config.houghTransform.rhoJump );
            Imgproc.HoughLines(cannyOutput, houghLines, config.houghTransform.rhoJump, config.houghTransform.thetaJump, config.houghTransform.threshold);
            //houghTransformRemoveSimilarLines();
            needToDoHughLines = false;
        }
    }
    @Deprecated
    private void houghTransformRemoveSimilarLines(){
        indexOfNonSimilarHoughLines = new ArrayList<>();
        for(int i=0;i<houghLines.rows();i++){
            double rho0 = houghLines.get(i, 0)[0],
                    theta0 = houghLines.get(i, 0)[1];
            boolean matchFound = false;
            for(int j=0;j<i;j++){
                double rho1 = houghLines.get(j, 0)[0],
                        theta1 = houghLines.get(j, 0)[1];
                //System.out.println("\n\n\n\n\ni: " + i + "\nj: " + j + "\nrho0: " + rho0 + "\nrho1: " + rho1 + "\ntheta0: " + theta0+ "\ntheta1: " + theta1);
                if(houghTransformCheckIfLinesAreNear(rho0, rho1, theta0, theta1)){
                    matchFound = true;
                    break;
                }
            }
            if(!matchFound){
                indexOfNonSimilarHoughLines.add(i);
            }
        }
    }
    @Deprecated
    private  boolean houghTransformCheckIfLinesAreNear(double rho0, double rho1, double theta0, double theta1){
        double dRho = rho0 - rho1;
        double dTheta = theta1 - theta0;
        return dRho < config.houghTransform.rhoSimilarityThreshold && dRho > -config.houghTransform.rhoSimilarityThreshold && dTheta < config.houghTransform.thetaSimilarityThreshold && dTheta > - config.houghTransform.thetaSimilarityThreshold;
    }

    @Deprecated
    public Mat getHoughLinesDrawing(BackgroundType backgroundType){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        doHoughLinesIfNecessary();
        return drawHoughLines(backgroundType);
    }
    @Deprecated
    private Mat drawHoughLines(BackgroundType backgroundType){
        Mat background = getMatFromBackgroundType(backgroundType);
        //for (int x : indexOfNonSimilarHoughLines) {
        for(int x=0;x<houghLines.rows();x++){
            double rho = houghLines.get(x, 0)[0],
                    theta = houghLines.get(x, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a*rho, y0 = b*rho;
            Point pt1 = new Point(Math.round(x0 + 10000*(-b)), Math.round(y0 + 10000*(a)));
            Point pt2 = new Point(Math.round(x0 - 10000*(-b)), Math.round(y0 - 10000*(a)));
            Imgproc.line(background, pt1, pt2, new Scalar(255, 0, 0), 3, Imgproc.LINE_AA, 0);
        }
        System.out.println();
        return background;

    }

    /*HoughLines_P************************************************************************/
    @Deprecated
    private void doHoughLinesIfNecessary_P(){
        if(needToDoHughLines_P){
            Imgproc.HoughLinesP(cannyOutput, houghLinesP, config.houghTransform.rhoJump, config.houghTransform.thetaJump,config.houghTransform.threshold,config.houghTransform.minLineLength,config.houghTransform.maxLineGap);
            needToDoHughLines_P = false;
        }
    }
    @Deprecated
    public Mat getHoughLinesDrawing_P(BackgroundType backgroundType){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        doHoughLinesIfNecessary_P();
        return drawHoughLines_P(backgroundType);
    }
    @Deprecated
    private Mat drawHoughLines_P(BackgroundType backgroundType){
        Mat background = getMatFromBackgroundType(backgroundType);
        for (int x = 0; x < houghLinesP.rows(); x++) {
            double[] l = houghLinesP.get(x, 0);
            Imgproc.line(background, new Point(l[0], l[1]), new Point(l[2], l[3]), randomColor(), 3, Imgproc.LINE_AA, 0);
        }
        return background;

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




    //http://felix.abecassis.me/2011/10/opencv-rotation-deskewing/, https://answers.opencv.org/question/497/extract-a-rotatedrect-area/
    @Deprecated
    private Mat cropToRotatedRect(RotatedRect rect){
        Mat M = new Mat();
        Mat rotated = new Mat();
        Mat cropped = new Mat();
        double angle = rect.angle;
        Size rectSize = rect.size;
        if(angle<-45.0){
            angle+=90;
            //swap the width and height
            double temp = rectSize.width;
            //noinspection SuspiciousNameCombination
            rectSize.width = rectSize.height;
            rectSize.height = temp;
        }
        M = Imgproc.getRotationMatrix2D(rect.center, angle, 1.0);
        Imgproc.warpAffine(initialMat, rotated,M, initialMat.size(), Imgproc.INTER_CUBIC);
        Imgproc.getRectSubPix(rotated, rectSize, rect.center, cropped);
        return cropped;
    }
    @Deprecated
    public Mat getContourDrawing(BackgroundType backgroundType){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        findContoursIfNecessary();
        return drawContours(backgroundType);
    }

    @Deprecated
    private Mat drawContours(BackgroundType backgroundType){
        Mat canvas = getMatFromBackgroundType(backgroundType);
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = randomColor();
            Imgproc.drawContours(canvas, contours, i, color, 2, Core.LINE_8, hierarchy, 0, new Point());
        }
        return canvas;
    }

    @Deprecated
    public Mat getContourBoxDrawing(BackgroundType backgroundType){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        findContoursIfNecessary();
        return drawContourBoxes(backgroundType);
    }
    @Deprecated
    private Mat drawContourBoxes(BackgroundType backgroundType){
        Mat canvas = getMatFromBackgroundType(backgroundType);
        for(RotatedRect rect:allCardRects){
            Scalar color = randomColor();
            Point[] points = new Point[4];
            rect.points(points);
            for(int i=0; i<4; ++i){
                Imgproc.line(canvas, points[i], points[(i+1)%4], color,3);
            }
        }
        return canvas;
    }
    @Deprecated
    public List<Mat> getCroppedCards(){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        findContoursIfNecessary();
        return croppedCards;
    }


    /*other functions************************************************************************/
    private Mat getMatFromBackgroundType(BackgroundType backgroundType){
        Size size = cannyOutput.size();
        switch (backgroundType){
            case EDGES:
                Mat colorClone = new Mat();
                Imgproc.cvtColor(cannyOutput, colorClone, Imgproc.COLOR_GRAY2BGR);
                return colorClone;
            case ORIGINAL_IMAGE:
                return initialMat.clone();
            case BLACK:
            default:
                return Mat.zeros(size, CvType.CV_8UC3);

        }
    }
    private Scalar randomColor(){
        return new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
    }


}
