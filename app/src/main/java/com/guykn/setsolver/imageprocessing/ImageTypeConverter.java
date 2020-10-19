package com.guykn.setsolver.imageprocessing;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.INTER_AREA;
import static org.opencv.imgproc.Imgproc.INTER_LINEAR;

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

    /**
     * Downscales a Mat so that it has the specified total number of pixels,
     * while still maintaining its aspect ratio.
     * @param src the Mat that needs to be scaled down.
     * @param targetTotalPixels How many total pixels the returned Mat should have.
     * @return a scaled down version of src.
     */
    public static Mat scaleDown(Mat src, int targetTotalPixels) {
        return scaleDown(src, targetTotalPixels, INTER_AREA);
    }

    /**
     * Downscales a Mat so that it has the specified total number of pixels,
     * while still maintaining its aspect ratio.
     * @param src the Mat that needs to be scaled down.
     * @param targetTotalPixels How many total pixels the returned Mat should have.
     * @return a scaled down version of src.
     */
    public static Mat scaleDown(Mat src, int targetTotalPixels, int interpolation ){
        double area = src.size().area();
        double areaScaleFactor = targetTotalPixels/area;
        //since we're adjusting both sides evenly, we need to scale based on the square root.
        double sideLengthScaleFactor = Math.sqrt(areaScaleFactor);

        int scaledDownRows = (int) (src.rows()*sideLengthScaleFactor);
        int scaledDownCols = (int) (src.cols()*sideLengthScaleFactor);

        Mat scaledDown = new Mat(scaledDownRows, scaledDownCols, src.type());
        Imgproc.resize(src, scaledDown, scaledDown.size(),0,0, interpolation);
        return scaledDown;
    }


}
