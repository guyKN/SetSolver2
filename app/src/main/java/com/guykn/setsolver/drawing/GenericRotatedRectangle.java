package com.guykn.setsolver.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
public class GenericRotatedRectangle implements DrawableOnCanvas {

    //todo: debug more: sometimes rectangles are clearly not quite right
    //todo: add comments to make it generally more clear

    public static final String TAG = "GenericRotatedRectangle";

    private int centerX;
    private int centerY;
    private int width;
    private int height;
    private double angle;

    private int originalCanvasWidth;
    private int originalCanvasHeight;

    private static final boolean WRITE_TO_CONSOLE = false;

    @Override
    public void drawOnCanvas(Canvas canvas, Paint paint) {

        ScaleFactor scaleFactor = new ScaleFactor(canvas.getWidth(), canvas.getHeight());

        if(WRITE_TO_CONSOLE) Log.d(TAG, angle > -45.0 ? "angle>-45":"-45>angle");
        if(WRITE_TO_CONSOLE) Log.d(TAG, (height>width ? "h>w": "w>h"));

        Rect rect = new Rect(
                (int) ((centerX - width/2)*scaleFactor.x),
                (int) ((centerY - height/2)*scaleFactor.y),
                (int) ((centerX + width/2)*scaleFactor.x),
                (int) ((centerY + height/2)*scaleFactor.y)
        );
        canvas.save();
        canvas.rotate(
                (float) angle,
                (float) (centerX*scaleFactor.x),
                (float) (centerY*scaleFactor.y)
        );

        canvas.drawRect(rect, paint);
        canvas.restore();

        if(WRITE_TO_CONSOLE)
            Log.i(TAG, "canvas width: " + canvas.getWidth()
                    + "canvas height: " + canvas.getHeight());
    }

    @Override
    public void saveToGallery(ImageFileManager fileManager, Mat originalImage) {
            try {
                Mat cropped = cropToRect(originalImage);
                fileManager.saveToGallery(cropped);
            }catch (IllegalArgumentException e){
                printState();
                throw e;
            }
    }

    public Mat cropToRect(Mat initialMat){
        //todo: check angles less than 45
        //todo: make sure this is working 100%
        Size originalImageSize = initialMat.size();
        int newWidth = (int) originalImageSize.width;
        int newHeight = (int) originalImageSize.height;

        ScaleFactor scaleFactor = new ScaleFactor(newWidth, newHeight);


        Mat M = new Mat();
        Mat rotated = new Mat();
        Mat cropped = new Mat();


        Point adjustedCenter = new Point(centerX*scaleFactor.x,
                centerY*scaleFactor.y);

        Size adjustedSize = new Size(width*scaleFactor.x,
                height*scaleFactor.y);

        M = Imgproc.getRotationMatrix2D(adjustedCenter, angle, 1.0);
        Imgproc.warpAffine(initialMat, rotated, M, initialMat.size(), Imgproc.INTER_CUBIC);
        Imgproc.getRectSubPix(rotated, adjustedSize, adjustedCenter, cropped);
        return cropped;
    }


    protected GenericRotatedRectangle(GenericRotatedRectangle rotatedRect) {
        this.centerX = rotatedRect.centerX;
        this.centerY = rotatedRect.centerY;
        this.width = rotatedRect.width;
        this.height = rotatedRect.height;
        this.angle = rotatedRect.angle;
        this.originalCanvasWidth = rotatedRect.originalCanvasWidth;
        this.originalCanvasHeight = rotatedRect.originalCanvasHeight;
    }

    public GenericRotatedRectangle(RotatedRect rect, int canvasWidth, int canvasHeight){
        centerX = (int) rect.center.x;
        centerY = (int) rect.center.y;
        width = (int) rect.size.width;
        height = (int) rect.size.height;
        angle = rect.angle;

        originalCanvasWidth = canvasWidth;
        originalCanvasHeight = canvasHeight;
    }

    public GenericRotatedRectangle(int centerX, int centerY, int width, int height, double angle, int originalCanvasWidth, int originalCanvasHeight){
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.angle = angle;
        this.originalCanvasWidth = originalCanvasWidth;
        this.originalCanvasHeight = originalCanvasHeight;
    }



    @Deprecated
    public  void drawOnCanvas3(Canvas canvas, Paint paint){
        Point[] corners = getCorners2();
        for(int i=0;i<corners.length;i++){
            int iNext = (i+1)%corners.length;
            canvas.drawLine(
                    (float) (corners[i].x*canvas.getWidth()),
                    (float) (corners[i].y*canvas.getHeight()),
                    (float)(corners[iNext].x * canvas.getWidth()),
                    (float) (corners[iNext].y*canvas.getHeight()),
                    paint
            );
        }

    }

    @Deprecated
    public Point[] getCornersTest(int canvasWidth, int CanvasHeight){
        return getCorners(canvasWidth, CanvasHeight);
    }

    @Deprecated
    private static double findAngle(Point A, Point B, Point C){
        double AB;
        double AC;
        double BC;
        AB = Math.sqrt(Math.pow(B.x - A.x, 2) + Math.pow(B.y - A.y, 2));
        AC = Math.sqrt(Math.pow(C.x - A.x, 2) + Math.pow(C.y - A.y, 2));
        BC = Math.sqrt(Math.pow(C.x - B.x, 2) + Math.pow(C.y - B.y, 2));
        double ratio = (AB * AB + AC * AC - BC * BC) /( 2 * AC * AB);
        double degrees = Math.acos(ratio)*(180/Math.PI);
        return degrees;

    }

    @Deprecated
    private Point[] getCorners2(){
        double _angle = angle/180*Math.PI;

        double b = (double) Math.cos(_angle) * 0.5;
        double a = (double) Math.sin(_angle) * 0.5;

        Point p0 = new Point(
                centerX - a * height - b * width,
                centerY + b * height - a * width
        );
        Point p1 = new Point(
                centerX + a * height - b * width,
                centerY - b * height - a * width
        );
        Point p2 = new Point(
                2 * centerX - p0.x,
                2 * centerY - p0.y
        );

        Point p3 = new Point(
                2 * centerX - p1.x,
                2 * centerY - p1.y
        );
        Log.d(TAG, "angle1: " + findAngle(p0,p1,p2) +
                "\nangle2: " + findAngle(p1,p2,p3) +
                "\nangle3: " + findAngle(p2,p3,p0) +
                "\nangle4: " + findAngle(p3,p0,p1) +
                "\ntest: " + findAngle(new Point(0,1),new Point(0,0),new Point(1,0))
        );
        return new Point[]{p0, p1, p2, p3};

    }

    @Deprecated
    private Point[] getCorners(int canvasWidth, int canvasHeight){
        printState();
        double angleRadians = angle/180*Math.PI;
        double sin = Math.sin(angleRadians);
        double cos = Math.cos(angleRadians);

        int segment1X = (int) ((width / 2 * cos) * canvasWidth);
        int segment1Y = (int) (((width / 2 * sin) * canvasHeight));

        int segment2X = (int) ((height / 2 * (-sin)) * canvasWidth);
        int segment2Y = (int) ((height / 2 * cos) * canvasHeight);

        int adjustedCenterX = (int) (centerX * canvasWidth);
        int adjustedCenterY = (int) (centerY * canvasHeight);


        if(WRITE_TO_CONSOLE) Log.d(TAG,
                String.format(Locale.US,
                        "\nangleRadians: %s\nsin: %s\ncos: %s\nsegment1X: %s\nsegment1Y: %s\nsegment2X: %s\nsegment2Y: %s\nadjustedCenterX: %s\nadjustedCenterY: %s",
                        angleRadians, sin, cos, segment1X, segment1Y, segment2X, segment2Y, adjustedCenterX, adjustedCenterY));

        Point p0 = new Point (adjustedCenterX + segment1X + segment2X,
                adjustedCenterY + segment1Y + segment2Y);
        Point p1 = new Point(adjustedCenterX + segment1X - segment2X,
                adjustedCenterY + segment1Y - segment2Y);
        Point p2 = new Point(adjustedCenterX - segment1X - segment2X,
                adjustedCenterY - segment1Y - segment2Y);
        Point p3 = new Point(adjustedCenterX - segment1X + segment2X,
                adjustedCenterY - segment1Y + segment2Y);

        return new Point[] {p0,p1,p2,p3};
    }

    /**
     * Prints all values in the class for debugging purposes
     */
    public void printState(){
        Log.d(TAG,
                String.format(Locale.US,
                        "\ncenterX: %s \ncenterY %s\nwidth: %s\nheight: %s\nangle: %s",
                        centerX, centerY, width, height, angle));

    }

    private class ScaleFactor{
        public double x;
        public double y;
        private ScaleFactor(int newCanvasWidth, int newCanvasHeight){
            this.x = ((double) newCanvasWidth)/( (double) originalCanvasWidth);
            this.y = ((double) newCanvasHeight)/( (double) originalCanvasHeight);
        }
    }

    /**
     * Currently doesn't work
     * todo: remove
     */
    @Deprecated
    private class Cropper {
        private int adjustedHeight;
        private int adjustedWidth;
        private Bitmap originalImage;

        Rect originalImageRect;

        Rect targetCropRect;
        public Cropper(Bitmap originalImage){
            this.originalImage = originalImage;
            int originalImageWidth = originalImage.getWidth();
            int originalImageHeight = originalImage.getHeight();

            originalImageRect = new Rect(
                    0,
                    0,
                    originalImageWidth,
                    originalImageHeight
            );

            int adjustedCenterX = (int) (centerX * originalImageWidth);
            int adjustedCenterY = (int) (centerY * originalImageHeight);
            adjustedWidth = (int) (width * originalImageWidth);
            adjustedHeight = (int) (height * originalImageHeight);

            targetCropRect = new Rect(
                    adjustedCenterX - adjustedWidth/2,
                    adjustedCenterY - adjustedHeight/2,
                    adjustedCenterX + adjustedWidth/2,
                    adjustedCenterY + adjustedHeight/2
            );
        }

        public Bitmap cropToRect() throws IllegalArgumentException{

            /*
            To do this, we first crop the bitmap down to a square that contains all possible rotations of the rectangle.
            If the square is outside of the range of the original bitmap, we fill in all blank spaces with black.
            Then, we rotate the new bitmap based on the angle.
            Finally, we crop this new bitmap exactly based on width and heigth.
             */
            Bitmap afterFirstCrop = doFirstCrop(originalImage);
            Bitmap rotated = rotate(afterFirstCrop);
            afterFirstCrop.recycle();
            Bitmap out = doSecondCrop(rotated);
            rotated.recycle();
            return out;
        }

        private Bitmap doFirstCrop(Bitmap src){
            /*
             the length of the diagonal is also the "radius" of the bitmap we're cropping to
             so it's half of it's width/height
            */

            int diagonalLength = (int) findDiagonalLength(targetCropRect.width()/2, targetCropRect.height()/2);

            Rect firstCropRect = new Rect(
                    targetCropRect.centerX() - diagonalLength,
                    targetCropRect.centerY() - diagonalLength,
                    targetCropRect.centerX() + diagonalLength,
                    targetCropRect.centerY() + diagonalLength
            );

            Bitmap cropped;

            /*
            check if rect we're trying to crop to is all within the image,
             if not, add a buffer zone, and crop it
             if yes, just crop it
            */
            if (originalImageRect.contains(firstCropRect)) {
                return Bitmap.createBitmap(
                        src,
                        firstCropRect.left,
                        firstCropRect.top,
                        firstCropRect.width(),
                        firstCropRect.height()
                );
            } else {
                Rect firstCropRectInbounds = new Rect();
                if(!firstCropRectInbounds.setIntersect(firstCropRect, originalImageRect)){
                    throw new IllegalArgumentException("the rectangles don't intersect, so something must have gone wrong.");
                }

                int destinationRectLeft = firstCropRectInbounds.left - firstCropRect.left;
                int destinationRectTop = firstCropRectInbounds.top - firstCropRect.top;
                Rect destinationRect = new Rect(
                        destinationRectLeft,
                        destinationRectTop,
                        destinationRectLeft+firstCropRectInbounds.width(),
                        destinationRectTop+firstCropRectInbounds.height()
                );

                Bitmap canvasBitmap = Bitmap.createBitmap(
                        diagonalLength*2,
                        diagonalLength *2,
                        Bitmap.Config.ARGB_8888
                ); //creates a blank bitmap to draw into

                Canvas canvas = new Canvas(canvasBitmap);
                canvas.drawBitmap(
                        src,
                        firstCropRectInbounds,
                        destinationRect,
                        null
                );
                return canvasBitmap;
            }
        }

        private Bitmap rotate(Bitmap src){
            int srcWidth = src.getWidth();
            int srcHeight = src.getHeight();

            Matrix rotation = new Matrix();
            rotation.setRotate(
                    (float) angle,
                    srcWidth/2f, //since the first crop always returns a bitmap whose center matches the center of the GenericRotatedRect,
                    srcHeight/2f // our width and height of the center of rotation should be half
            );

            Bitmap rotated = Bitmap.createBitmap(
                    originalImage,
                    0,
                    0,
                    srcWidth,
                    srcHeight,
                    rotation,
                    true
            );
            return rotated;
        }

        private Bitmap doSecondCrop(Bitmap src){
            return Bitmap.createBitmap(
                    src,
                    targetCropRect.left,
                    targetCropRect.top,
                    targetCropRect.width(),
                    targetCropRect.height());
        }

        /**
         * Finds the diagonal of a rectangle using pythagarous's theorem.
         * @param width the width of
         * @param height the height of the rectangle
         * @return the diagonal of the rectangle
         */
        private double findDiagonalLength(int width, int height){
            return Math.sqrt(
                    Math.pow(width, 2) +
                    Math.pow(height, 2)
            );
        }
    }

}