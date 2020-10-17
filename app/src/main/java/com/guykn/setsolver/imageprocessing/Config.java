package com.guykn.setsolver.imageprocessing;

import org.opencv.imgproc.Imgproc;

public class Config {

    public static class Image{
        public int width =1000;
        public int height = width*16/9;
    }
    public static class GaussianBlur{
        public int radius = 3;

    }
    public static class CannyEdgeDetection{
        public int threshold = 50;
        public float ratio = 2.0f;
    }

    public static class Contours{
        public int minContourPerimeter = 50;
        public int minContourArea = 200;
        public int reBlurRadius = 5;
        public double epsilonMultiplier = 0.1;
        public boolean useEpsilon = true;
        public int hierarchyType = Imgproc.RETR_EXTERNAL;
    }

    @Deprecated
    public static class HoughTransform{
        public int threshold = 100;
        public int minLineLength = 50;
        public int maxLineGap = 10;
        public double rhoSimilarityThreshold=30.0;
        public int rhoJump =1;
        public double thetaSimilarityThreshold = Math.PI/10;
        public double thetaJump = Math.PI/180;
    }
    public Image image = new Image();
    public GaussianBlur gaussianBlur = new GaussianBlur();
    public CannyEdgeDetection cannyEdgeDetection = new CannyEdgeDetection();
    public Contours contours = new Contours();
    @Deprecated
    public HoughTransform houghTransform = new HoughTransform();

    public static Config getDefaultConfig(){
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
