package com.guykn.setsolver.imageprocessing;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public final class ImageTypeConverter {

    /**
     * The constructor is private, and unreachable, since this class only contains helper methods.
     */
    private ImageTypeConverter(){}

    /**
     * Given a bitmap, returns a mutable Bitmap that is otherwise exactly the save
     * @param src A bitmap
     * @return A mutable bitmap that's otherwise the same as src.
     */
    public static Bitmap copyBitmapAsMutable(Bitmap src){
        return src.copy(src.getConfig(), true);
    }

    public static Mat nv21ToRgbMat(byte[] data, int width, int height){
        Mat nv21Mat = new Mat(height+height/2, width, CvType.CV_8UC1);
        Mat rgbMat = new Mat(nv21Mat.height(), nv21Mat.width(), CvType.CV_8UC1);
        nv21Mat.put(0,0, data);
        Imgproc.cvtColor(nv21Mat, rgbMat, Imgproc.COLOR_YUV2RGB_NV21);
        return rgbMat;
    }

    public static Bitmap matToBitmap(Mat mat){
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public static Mat bitmapToMat(Bitmap bitmap){
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        return mat;
    }


}
