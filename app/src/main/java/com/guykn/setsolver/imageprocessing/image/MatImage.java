package com.guykn.setsolver.imageprocessing.image;

import org.opencv.core.Mat;

public class MatImage extends Image{
    private final Mat mat;

    public MatImage(Mat mat){
        this.mat = mat;
    }

    @Override
    public Mat toMat() {
        return mat;
    }
}
