package com.guykn.setsolver.imageprocessing;

import com.guykn.setsolver.threading.ImageProcessingThreadManager.ImageProcessingAction;

import org.opencv.imgproc.Imgproc;

public class Config {

    public static class Image{
        public int totalPixels = 1000000;
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

    public enum ShouldSaveToGallery {
        ALWAYS{
            @Override
            public boolean shouldSaveToGallery(ImageProcessingAction action) {
                return true;
            }
        },
        ONLY_WHEN_PICTURE_IS_TAKEN{
            @Override
            public boolean shouldSaveToGallery(ImageProcessingAction action) {
                return action == ImageProcessingAction.DETECT_AND_CLASSIFY_CARDS;
            }
        },
        NEVER{
            @Override
            public boolean shouldSaveToGallery(ImageProcessingAction action) {
                return false;
            }
        };
        public abstract boolean shouldSaveToGallery(ImageProcessingAction action);
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
    public ShouldSaveToGallery shouldSaveToGallery = ShouldSaveToGallery.NEVER;
    @Deprecated
    public HoughTransform houghTransform = new HoughTransform();

    public static Config getDefaultConfig(){
        Config cfg = new Config();
        cfg.image.totalPixels = 1000000;

        cfg.gaussianBlur.radius = 7;
        cfg.cannyEdgeDetection.threshold = 50;
        cfg.cannyEdgeDetection.ratio = 2.0f;

        cfg.contours.useEpsilon = false;
        cfg.contours.reBlurRadius = 11;
        cfg.contours.minContourPerimeter = 200;
        cfg.contours.minContourArea = 1000;
        cfg.contours.hierarchyType = Imgproc.RETR_EXTERNAL;
        cfg.shouldSaveToGallery = ShouldSaveToGallery.ALWAYS;
        return cfg;
    }

}
