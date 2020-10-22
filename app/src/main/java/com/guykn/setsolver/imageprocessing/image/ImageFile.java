package com.guykn.setsolver.imageprocessing.image;

import android.content.Context;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.imageprocessing.StandardImagePreprocessor;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ImageFile extends Image {
    private final String path;

    public ImageFile(String path){
        this.path = path;
    }

    @Override
    public Mat toMat() {
        return Imgcodecs.imread(path);
    }
}
