package com.guykn.setsolver.imageprocessing.image;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
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

    public Bitmap toBitmap(){
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }
}