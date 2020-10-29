package com.guykn.setsolver.imageprocessing;

import com.guykn.setsolver.threading.deprecated.ImageProcessingThreadManager.ImageProcessingAction;

import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageProcessingConfig {

    //todo: tweak
    //todo: add fragment that allows user to change this

    public static class Image{
        public int totalPixels = 1000000;
        public Size scaledDownSize = new Size(256,256);
    }
    public static class GaussianBlur{
        public int radius = 7;
    }
    public static class CannyEdgeDetection{
        public int threshold = 50;
        public float ratio = 2.0f;
    }

    public static class Contours{
        public int reBlurRadius = 11;
        public int hierarchyType = Imgproc.RETR_EXTERNAL; //todo: see with one is best.
    }

    public static class ContourVerification{
        public boolean useContourPerimeter = false;
        public int minContourPerimeter = 0;

        public boolean useEpsilon = false;
        public double epsilonMultiplier = 0;

        public boolean useCardAspectRatioCheck = true;
        public double maxCardAspectRatioDeviation = 3;

        public int minContourArea = 5000;
        public boolean useAreaCheck=true;

        public boolean shouldDoOutlierDetection  = true;
        public double maxDistance = 1000000000;


    }


    public static class MemoryManagement {
        public boolean shouldReleaseUnprocessedImage = false;
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



    public Image image = new Image();
    public GaussianBlur gaussianBlur = new GaussianBlur();
    public CannyEdgeDetection cannyEdgeDetection = new CannyEdgeDetection();
    public Contours contours = new Contours();
    public ContourVerification contourVerification = new ContourVerification();
    public ShouldSaveToGallery shouldSaveToGallery = ShouldSaveToGallery.NEVER;
    public MemoryManagement memoryManagement= new MemoryManagement();


    public static ImageProcessingConfig getDefaultConfig(){
        ImageProcessingConfig cfg = new ImageProcessingConfig();
        return cfg;
    }

}
