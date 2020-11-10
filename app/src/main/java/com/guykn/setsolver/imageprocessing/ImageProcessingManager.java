package com.guykn.setsolver.imageprocessing;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.image.Image;
import com.guykn.setsolver.set.SetBoardPosition;

import org.opencv.core.Mat;

public interface ImageProcessingManager {
    public void setImage(Image image);
    public RotatedRectangleList getCardPositions();
    public SetBoardPosition getBoard();
    public void saveCardImagesToGallery(ImageFileManager fileManager);
    public void finish();

    interface ImagePreProcessor{
        public Mat preProcess(Mat mat, ImageProcessingConfig config);
    }

    public interface ImageProcessingManagerBuilder {
        public ImageProcessingManager build(ImageProcessingConfig config);
    }
}
