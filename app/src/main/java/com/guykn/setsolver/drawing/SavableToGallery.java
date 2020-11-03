package com.guykn.setsolver.drawing;

import com.guykn.setsolver.ImageFileManager;

import org.opencv.core.Mat;
import org.opencv.core.Size;

public interface SavableToGallery {
    public void saveToGallery(ImageFileManager fileManager, Mat originalImage);
    public void saveToGallery(ImageFileManager fileManager, Mat originalImage, Size scaledDownSize);
}
