package com.guykn.setsolver.imageprocessing;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.image.Image;

import org.opencv.core.Mat;

public interface ImageProcessingManager {

    public void setConfig(ImageProcessingConfig config);
    public void setImage(Image image);
    public RotatedRectangleList getCardPositions();
    public void saveCardImagesToGallery(ImageFileManager fileManager);
    public void saveOriginalImageToGallery(ImageFileManager fileManager);
    public void finish();

    interface ImagePreProcessor{
        public Mat preProcess(Mat mat, ImageProcessingConfig config);
    }
}
