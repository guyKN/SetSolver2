package com.guykn.setsolver.imageprocessing;


import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageProcessingConfig {

    //todo: tweak
    //todo: add fragment that allows user to change this

    public static class Image{
        public int totalPixels = 1000000;
        public int scaledDownWidth = 254;
        public int scaledDownHeight = 254;
        public Size getScaledDownSize(){
            return new Size(scaledDownWidth, scaledDownHeight);
        }
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
        public double maxDistance = 100000000;

        public boolean shouldDoAverageColorCheck = false;
        public double averageColorCheckThreshold = 80;
    }

    public static class MachineLearning{

    }


    public static class MemoryManagement {
        public boolean shouldReleaseUnprocessedImage = false;
    }

    public static class FpsCounting{
        public long fpsUpdateInterval = 300;
    }



    public Image image = new Image();
    public GaussianBlur gaussianBlur = new GaussianBlur();
    public CannyEdgeDetection cannyEdgeDetection = new CannyEdgeDetection();
    public Contours contours = new Contours();
    public ContourVerification contourVerification = new ContourVerification();
    public MemoryManagement memoryManagement= new MemoryManagement();
    public FpsCounting fpsCounting = new FpsCounting();



    public static ImageProcessingConfig getDefaultConfig(){
        ImageProcessingConfig cfg = new ImageProcessingConfig();
        return cfg;
    }

}