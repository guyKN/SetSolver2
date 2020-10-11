package com.guykn.setsolver.imageprocessing;

import android.content.Context;

import com.guykn.setsolver.imageprocessing.classifiers.CardClassifier;
import com.guykn.setsolver.set.SetCard;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SetCardFinder {
    //todo: remove Hough transform code, remove the setConfig function since there's no need to change config.
    //todo: manage memory: make sure only things that are necessary are loaded into memory.
    //todo: make an interface to encompass important things about this class, and implement that interface here
    private Mat initialMat = new Mat();
    private Mat blurredMat = new Mat(); //same as initialMat, but in greyscale and with a gausian filter
    private Mat hierarchy = new Mat();
    private Mat cannyOutput = new Mat();
    private Mat houghLinesP = new Mat(); //lines after the P hough transform
    private Mat houghLines = new Mat();
    private List<Integer> indexOfNonSimilarHoughLines;
    private List<RotatedRect> allCardRects;
    private List<MatOfPoint> contours;
    private List<Mat> croppedCards;

    private boolean needToDoGaussianFilter;
    private boolean needToDoCanny;
    private boolean needToFindContours;
    private boolean needToDoHughLines_P;
    private boolean needToDoHughLines;

    private String imagePath;
    private Config config;
    private CardClassifier classifier;
    private Random rng = new Random(12789);
    private Context context;

    public enum BackgroundType {BLACK, ORIGINAL_IMAGE, EDGES}

    public SetCardFinder(String imagePath, Config config, Context context) throws IOException {
        this.config = config;
        this.imagePath = imagePath;
        loadImage();
        needToDoGaussianFilter = true;
        needToDoCanny = true;
        needToFindContours = true;
        needToDoHughLines_P = true;
        needToDoHughLines = true;
        classifier = new CardClassifier(context, config);
    }

    public void setConfig(Config newConfig){
        if(!newConfig.image.equals(config.image)){
            loadImage();
            needToDoGaussianFilter = true;
            needToDoCanny = true;
            needToFindContours = true;
            needToDoHughLines_P = true;
            needToDoHughLines = true;
        }
        if(!newConfig.gaussianBlur.equals(config.gaussianBlur)){
            needToDoGaussianFilter = true;
            needToDoCanny = true;
            needToFindContours = true;
            needToDoHughLines_P = true;
            needToDoHughLines = true;
        }
        if(!newConfig.cannyEdgeDetection.equals(config.cannyEdgeDetection)){
            needToDoCanny = true;
            needToFindContours=true;
            needToDoHughLines_P = true;
            needToDoHughLines = true;
        }
        if(!newConfig.contours.equals(config.contours)){
            needToFindContours=true;
        }
        if(!newConfig.houghTransform.equals(config.houghTransform)){
            needToDoHughLines = true;
            needToDoHughLines_P = true;
        }
        config = newConfig;
    }

    private void loadImage(){
        Mat fullImage = Imgcodecs.imread(imagePath);
        Size scaledDownSize = new Size(config.image.width,config.image.height);
        Imgproc.resize(fullImage, initialMat, scaledDownSize, 0,0, Imgproc.INTER_AREA);
    }

    /*Gausinan Filter************************************************************************/
    private void doGaussianFilterIfNecessary(){
        if(needToDoGaussianFilter){
            Imgproc.cvtColor(initialMat, blurredMat, Imgproc.COLOR_BGR2GRAY); //convert to greyscale
            Imgproc.blur(blurredMat, blurredMat, new Size(config.gaussianBlur.radius, config.gaussianBlur.radius));//users gausian blur to remove noise.
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
            Imgproc.Canny(blurredMat, cannyOutput, config.cannyEdgeDetection.threshold, config.cannyEdgeDetection.ratio*config.cannyEdgeDetection.threshold);
            needToDoCanny = false;
        }
    }
    public Mat getCannyOutput(){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        return cannyOutput;
    }

    /*HoughLines************************************************************************/
    private void doHoughLinesIfNecessary(){
        if(needToDoHughLines){
            System.out.println("inside function. RhoJump=" + config.houghTransform.rhoJump );
            Imgproc.HoughLines(cannyOutput, houghLines, config.houghTransform.rhoJump, config.houghTransform.thetaJump, config.houghTransform.threshold);
            //houghTransformRemoveSimilarLines(); //todo: add back
            needToDoHughLines = false;
        }
    }
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
    private  boolean houghTransformCheckIfLinesAreNear(double rho0, double rho1, double theta0, double theta1){
        double dRho = rho0 - rho1;
        double dTheta = theta1 - theta0;
        return dRho < config.houghTransform.rhoSimilarityThreshold && dRho > -config.houghTransform.rhoSimilarityThreshold && dTheta < config.houghTransform.thetaSimilarityThreshold && dTheta > - config.houghTransform.thetaSimilarityThreshold;
    }

    public Mat getHoughLinesDrawing(BackgroundType backgroundType){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        doHoughLinesIfNecessary();
        return drawHoughLines(backgroundType);
    }
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
    private void doHoughLinesIfNecessary_P(){
        if(needToDoHughLines_P){
            Imgproc.HoughLinesP(cannyOutput, houghLinesP, config.houghTransform.rhoJump, config.houghTransform.thetaJump,config.houghTransform.threshold,config.houghTransform.minLineLength,config.houghTransform.maxLineGap);
            needToDoHughLines_P = false;
        }
    }
    public Mat getHoughLinesDrawing_P(BackgroundType backgroundType){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        doHoughLinesIfNecessary_P();
        return drawHoughLines_P(backgroundType);
    }
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
            contours = new ArrayList<>();
            Mat reBlurredCanny = new Mat();
            Imgproc.blur(cannyOutput, reBlurredCanny, new Size(config.contours.reBlurRadius, config.contours.reBlurRadius));//re-apply a gausian blur to remove noise in the edges
            Imgproc.findContours(reBlurredCanny, contours, hierarchy, config.contours.hierarchyType, Imgproc.CHAIN_APPROX_SIMPLE);
            findRectsFromContours();
            needToFindContours = false;
        }
    }
    private void findRectsFromContours() {
        allCardRects = new ArrayList<RotatedRect>();
        croppedCards = new ArrayList<>();
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
                    allCardRects.add(rotatedRect);
                    Mat cropped = cropToRotatedRect(rotatedRect);
                    croppedCards.add(cropped);
                }
            }
        }
    }
    //the below function is wrong but still being preserved
    //http://felix.abecassis.me/2011/10/opencv-rotation-deskewing/, https://answers.opencv.org/question/497/extract-a-rotatedrect-area/
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
            rectSize.width = rectSize.height;
            rectSize.height = temp;
        }
        M = Imgproc.getRotationMatrix2D(rect.center, angle, 1.0);
        Imgproc.warpAffine(initialMat, rotated,M, initialMat.size(), Imgproc.INTER_CUBIC);
        Imgproc.getRectSubPix(rotated, rectSize, rect.center, cropped);
        return cropped;
    }
    public Mat getContourDrawing(BackgroundType backgroundType){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        findContoursIfNecessary();
        return drawContours(backgroundType);
    }
    private Mat drawContours(BackgroundType backgroundType){
        Mat canvas = getMatFromBackgroundType(backgroundType);
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = randomColor();
            Imgproc.drawContours(canvas, contours, i, color, 2, Core.LINE_8, hierarchy, 0, new Point());
        }
        return canvas;
    }

    public Mat getContourBoxDrawing(BackgroundType backgroundType){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        findContoursIfNecessary();
        return drawContourBoxes(backgroundType);
    }
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
    public List<Mat> getCroppedCards(){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        findContoursIfNecessary();
        return croppedCards;
    }

    /*Card classification*******************************************************************/
    public SetCard getCardClassifications(){
        doGaussianFilterIfNecessary();
        doCannyEdgeDetectionIfNecessary();
        findContoursIfNecessary();
        return classifier.classify(croppedCards.get(0));
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

    public static class Config {

        public static class Image{
            public int width =1000;
            public int height = width*16/9;
            public boolean equals(Image image){
                return this.height == image.height && this.width == image.width;
            }
        }
        public static class GaussianBlur{
            public int radius = 3;
            public boolean equals(GaussianBlur gaussianBlur){
                return this.radius == gaussianBlur.radius;
            }

        }
        public static class CannyEdgeDetection{
            public int threshold = 50;
            public float ratio = 2.0f;
            public boolean equals(CannyEdgeDetection cannyEdgeDetection){
                return this.threshold == cannyEdgeDetection.threshold && this.ratio == cannyEdgeDetection.ratio;
            }

        }

        public static class Contours{
            public int minContourPerimeter = 50;
            public int minContourArea = 200;
            public int reBlurRadius = 5;
            public double epsilonMultiplier = 0.1;
            public boolean useEpsilon = true;
            public int hierarchyType = Imgproc.RETR_EXTERNAL;
            public boolean equals(Contours contours){
                return this.minContourPerimeter == contours.minContourPerimeter && this.reBlurRadius == contours.reBlurRadius && this.epsilonMultiplier == contours.epsilonMultiplier && this.useEpsilon == contours.useEpsilon;
            }
        }

        public static class HoughTransform{
            public int threshold = 100;
            public int minLineLength = 50;
            public int maxLineGap = 10;
            public double rhoSimilarityThreshold=30.0;
            public int rhoJump =1;
            public double thetaSimilarityThreshold = Math.PI/10;
            public double thetaJump = Math.PI/180;
            public boolean equals(HoughTransform houghTransform){ //todo: make it count other changed stuff
                return this.threshold == houghTransform.threshold && this.minLineLength == houghTransform.minLineLength && this.maxLineGap == houghTransform.maxLineGap;
            }
        }
        public Image image = new Image();
        public GaussianBlur gaussianBlur = new GaussianBlur();
        public CannyEdgeDetection cannyEdgeDetection = new CannyEdgeDetection();
        public Contours contours = new Contours();
        public HoughTransform houghTransform = new HoughTransform();

        public static Config  getDefaultConfig(){
            Config cfg = new Config();
            cfg.image.height=1000;
            cfg.image.width = 1000;

            cfg.gaussianBlur.radius = 7;
            cfg.cannyEdgeDetection.threshold = 50;
            cfg.cannyEdgeDetection.ratio = 2.0f;

            cfg.contours.useEpsilon = false;
            cfg.contours.reBlurRadius = 11;
            cfg.contours.minContourPerimeter = 200;
            cfg.contours.minContourArea = 1000;
            cfg.contours.hierarchyType = Imgproc.RETR_EXTERNAL;

            return cfg;
        }

    }



}
