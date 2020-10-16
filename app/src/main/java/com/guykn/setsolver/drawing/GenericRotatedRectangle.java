package com.guykn.setsolver.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.MainActivity;

import org.opencv.core.RotatedRect;

import java.util.Locale;

/**
 * Stores the location of a rotated rectangle in an image (Primary used to track where set cards are)
 * Is generic, meaning all values are stored as doubles from 0 to 1. This ensures that conversion between different canvases is easy.
 */
public class GenericRotatedRectangle implements DrawableOnCanvas {

    //todo: make delete comments completely, or add a boolean to check if they're needed

    private double centerX;
    private double centerY;
    private double width;
    private double height;
    private double angle;


    @Override
    public void drawOnCanvas(Canvas canvas, Paint paint) {
        Log.i(DrawableOnCanvas.TAG, "canvas width: " + canvas.getWidth() + "canvas height: " + canvas.getHeight());
        Point[] corners = getCorners(canvas.getWidth(), canvas.getHeight());
        for(int i=0;i<corners.length;i++){
            int iNext = (i+1)%corners.length;
            canvas.drawLine(
                    corners[i].x,
                    corners[i].y,
                    corners[iNext].x,
                    corners[iNext].y,
                    paint
            );
        }
    }

    @Override
    public void saveToGallery(ImageFileManager fileManager, Bitmap originalImage) {
            try {
                Bitmap cropped = cropToRect(originalImage);
                fileManager.saveToGallery(cropped);
            }catch (IllegalArgumentException e){
                printState();
                e.printStackTrace();
            }
    }

    public Bitmap cropToRect(Bitmap originalImage){
        Cropper cropper = new Cropper(originalImage);
        return cropper.cropToRect();
    }

    @Deprecated
    public Point[] getCornersTest(int canvasWidth, int CanvasHeight){
        return getCorners(canvasWidth, CanvasHeight);
    }

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

        /*
        Log.d(MainActivity.TAG,
                String.format(Locale.US,
                        "\nangleRadians: %s\nsin: %s\ncos: %s\nsegment1X: %s\nsegment1Y: %s\nsegment2X: %s\nsegment2Y: %s\nadjustedCenterX: %s\nadjustedCenterY: %s",
                        angleRadians, sin, cos, segment1X, segment1Y, segment2X, segment2Y, adjustedCenterX, adjustedCenterY));
        */
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



    protected GenericRotatedRectangle(GenericRotatedRectangle rotatedRect) {
        this.centerX = rotatedRect.centerX;
        this.centerY = rotatedRect.centerY;
        this.width = rotatedRect.width;
        this.height = rotatedRect.height;
        this.angle = rotatedRect.angle;
    }

    public GenericRotatedRectangle(RotatedRect rect, int canvasWidth, int canvasHeight){
        centerX = ( (double) rect.center.x) / ( (double) canvasWidth);
        centerY = ( (double) rect.center.y) / ( (double) canvasHeight);
        width = ( (double) rect.size.width )/( (double) canvasWidth);
        height = ( (double) rect.size.height) / ( (double) canvasHeight);
        angle = rect.angle;
    }

    public GenericRotatedRectangle(double centerX, double centerY, double width, double height, double angle){
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.angle = angle;
    }

    /**
     * Prints all values in the class for debugging purposes
     */
    public void printState(){
        Log.d(MainActivity.TAG,
                String.format(Locale.US,
                        "\ncenterX: %s \ncenterY %s\nwidth: %s\nheight: %s\nangle: %s",
                        centerX, centerY, width, height, angle));

    }

    private class Cropper {
        //todo: optimize performance by doing 1 transformation
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
            //todo: make this work actually

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
            rotated.recycle(); //todo: make sure we're not recycling badly by accident
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