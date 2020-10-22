package com.guykn.setsolver.imageprocessing.image;

import org.opencv.core.Mat;

/**
 * Abstract class that represents an Image that can be converted to an OpenCv mat
 */
public abstract class Image {
    public abstract Mat toMat();
}
