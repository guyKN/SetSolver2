package com.guykn.setsolver.imageprocessing.image;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class JpegByteArrayImage extends Image{
    private final byte[] data;
    private final int width;
    private final int height;

    public JpegByteArrayImage(byte[] data, int width, int height){
        this.data = data;
        this.width = width;
        this.height = height;
    }

    //@Override
    public Mat toMat() {
        //todo: make more efficient
        Mat mat = Imgcodecs.imdecode(new MatOfByte(data), Imgcodecs.IMREAD_COLOR);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB, 4);
        return mat;
    }
}
