package com.guykn.setsolver.imageprocessing.image;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class BitmapImage extends Image{
    private Bitmap bitmap;

    public BitmapImage(Bitmap bitmap){

        this.bitmap = bitmap;
    }

    @Override
    public Mat toMat() {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        return mat;
    }
}
