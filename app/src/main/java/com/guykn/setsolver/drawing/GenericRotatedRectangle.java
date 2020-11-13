package com.guykn.setsolver.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.guykn.setsolver.ImageFileManager;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Locale;

/**
 * Stores the location of a rotated rectangle in an image (Primary used to track where set cards are)
 * Is generic, meaning all values are stored as doubles from 0 to 1. This ensures that conversion between different canvases is easy.
 */
public class GenericRotatedRectangle implements DrawingCallback, SavableToGallery {

    // todo: add comments to make it generally more clear
    // todo: rotate so that width is always greater than height
    // todo: crop a little bit into the image so that the edges are removed

    public static final String TAG = "GenericRotatedRectangle";

    final protected int centerX;
    final protected int centerY;
    final protected int width;
    final protected int height;
    final private double angle;

    private int originalCanvasWidth;
    private int originalCanvasHeight;

    private Rect adjustedCardRect;
    private Point adjustedCenterPoint;

    private int area;

    private static final boolean WRITE_TO_CONSOLE = false;

    protected Rect getAdjustedCardRect() {
        return adjustedCardRect;
    }

    protected Point getAdjustedCenterPoint() {
        return adjustedCenterPoint;
    }

    @Override
    public void onSizeChange(int newCanvasWidth, int newCanvasHeight) {
        if(WRITE_TO_CONSOLE) {
            Log.d(TAG, "called OnsizeChanged()!");
            Log.d(TAG, "onSizeChange() width = " + newCanvasWidth + "height = " + newCanvasHeight);
        }
        ScaleFactor scaleFactor = new ScaleFactor(newCanvasWidth, newCanvasHeight);



        adjustedCardRect = new Rect(
                (int) ((centerX - this.width / 2) * scaleFactor.x),
                (int) ((centerY - this.height / 2) * scaleFactor.y),
                (int) ((centerX + this.width / 2) * scaleFactor.x),
                (int) ((centerY + this.height / 2) * scaleFactor.y)
        );

        Log.d(TAG, adjustedCardRect.toString());



        adjustedCenterPoint = new Point(
                centerX * scaleFactor.x,
                centerY * scaleFactor.y
        );

    }

    @Override
    public final void drawOnCanvas(Canvas canvas, Paint paint) {


        if (WRITE_TO_CONSOLE) Log.d(TAG, angle > -45.0 ? "angle>-45" : "-45>angle");
        if (WRITE_TO_CONSOLE) Log.d(TAG, (height > width ? "h>w" : "w>h"));


        canvas.save();
        canvas.rotate(
                (float) angle,
                (float) adjustedCenterPoint.x,
                (float) adjustedCenterPoint.y
        );
        drawOnCanvasRotated(canvas, paint);
        canvas.restore();

        if (WRITE_TO_CONSOLE)
            Log.i(TAG, "canvas width: " + canvas.getWidth()
                    + "canvas height: " + canvas.getHeight());
    }

    /**
     * Called Internally after the canvas is rotated to do the drawing.
     */
    protected void drawOnCanvasRotated(Canvas canvas, Paint paint) {
        canvas.drawRect(getAdjustedCardRect(), paint);
    }


    @Override
    public void saveToGallery(ImageFileManager fileManager, Mat originalImage) {
        try {
            Mat cropped = cropToRect(originalImage);
            fileManager.saveToGallery(cropped);
        } catch (IllegalArgumentException e) {
            printState();
            throw e;
        }
    }

    @Override
    public void saveToGallery(ImageFileManager fileManager, Mat originalImage, Size scaledDownSize) {
        try {
            Mat cropped = cropToRect(originalImage, scaledDownSize);
            fileManager.saveToGallery(cropped);
        } catch (IllegalArgumentException e) {
            printState();
            throw e;
        }
    }

    public Mat cropToRect(Mat initialMat) {
        Size originalImageSize = initialMat.size();
        int newWidth = (int) originalImageSize.width;
        int newHeight = (int) originalImageSize.height;

        ScaleFactor scaleFactor = new ScaleFactor(newWidth, newHeight);


        Mat M = new Mat();
        Mat rotated = new Mat();
        Mat cropped = new Mat();


        Point adjustedCenter = new Point(centerX * scaleFactor.x,
                centerY * scaleFactor.y);

        Size adjustedSize = new Size(width * scaleFactor.x,
                height * scaleFactor.y);

        M = Imgproc.getRotationMatrix2D(adjustedCenter, angle, 1.0);
        Imgproc.warpAffine(initialMat, rotated, M, initialMat.size(), Imgproc.INTER_CUBIC);
        Imgproc.getRectSubPix(rotated, adjustedSize, adjustedCenter, cropped);
        return cropped;
    }

    public Mat cropToRect(Mat initialMat, Size size) {
        Mat cropped = cropToRect(initialMat);

        Mat resized = new Mat();
        Imgproc.resize(cropped, resized, size);
        return resized;
    }


    public int getArea() {
        return area;
    }

    protected GenericRotatedRectangle(GenericRotatedRectangle rotatedRect) {
        this.centerX = rotatedRect.centerX;
        this.centerY = rotatedRect.centerY;
        this.width = rotatedRect.width;
        this.height = rotatedRect.height;
        this.angle = rotatedRect.angle;
        this.originalCanvasWidth = rotatedRect.originalCanvasWidth;
        this.originalCanvasHeight = rotatedRect.originalCanvasHeight;
        this.area = rotatedRect.area;
    }

    private GenericRotatedRectangle(RotatedRect rect) {
        centerX = (int) rect.center.x;
        centerY = (int) rect.center.y;
        width = (int) rect.size.width;
        height = (int) rect.size.height;
        angle = rect.angle;
    }

    public GenericRotatedRectangle(RotatedRect rect, int canvasWidth, int canvasHeight) {
        this(rect);
        originalCanvasWidth = canvasWidth;
        originalCanvasHeight = canvasHeight;
        this.area = width * height;
    }

    public GenericRotatedRectangle(RotatedRect rect, int canvasWidth, int canvasHeight, int area) {
        this(rect, canvasWidth, canvasHeight);
        originalCanvasWidth = canvasWidth;
        originalCanvasHeight = canvasHeight;
        this.area = area;
    }


    /**
     * Prints all values in the class for debugging purposes
     */
    public void printState() {
        Log.d(TAG,
                String.format(Locale.US,
                        "\ncenterX: %s \ncenterY %s\nwidth: %s\nheight: %s\nangle: %s",
                        centerX, centerY, width, height, angle));

    }

    private class ScaleFactor {
        public double x;
        public double y;

        private ScaleFactor(int newCanvasWidth, int newCanvasHeight) {
            this.x = ((double) newCanvasWidth) / ((double) originalCanvasWidth);
            this.y = ((double) newCanvasHeight) / ((double) originalCanvasHeight);
        }
    }

}