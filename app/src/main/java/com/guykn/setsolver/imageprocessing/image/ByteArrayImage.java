package com.guykn.setsolver.imageprocessing.image;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ByteArrayImage extends Image {
    private final byte[] data;
    private final int width;
    private final int height;

    public ByteArrayImage(byte[] data, int width, int height){
        this.data = data;
        this.width = width;
        this.height = height;
    }

    @Override
    public Mat toMat() {
        return nv21ToRgbMat(data, width, height);
    }

    public static Mat nv21ToRgbMat(byte[] data, int width, int height){
        Mat nv21Mat = new Mat(height+height/2, width, CvType.CV_8UC1);
        Mat rgbMat = new Mat(nv21Mat.height(), nv21Mat.width(), CvType.CV_8UC1);
        nv21Mat.put(0,0, data);
        Imgproc.cvtColor(nv21Mat, rgbMat, Imgproc.COLOR_YUV2RGB_NV21);
        return rgbMat;
    }

}
