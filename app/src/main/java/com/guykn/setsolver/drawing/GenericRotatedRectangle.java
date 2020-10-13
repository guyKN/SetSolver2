package com.guykn.setsolver.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

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

    @Deprecated
    public Point[] getCornersTest(int canvasWidth, int CanvasHeight){
        return getCorners(canvasWidth, CanvasHeight);
    }

    private Point[] getCorners(int canvasWidth, int canvasHeight){
        double angleRadians = angle/180*Math.PI;
        double sin = Math.sin(angleRadians);
        double cos = Math.cos(angleRadians);

        int segment1X = (int) ((width / 2 * cos) * canvasWidth);
        int segment1Y = (int) (((width / 2 * sin) * canvasHeight));

        int segment2X = (int) ((height / 2 * (-sin)) * canvasWidth);
        int segment2Y = (int) ((height / 2 * cos) * canvasHeight);

        int adjustedCenterX = (int) (centerX * canvasWidth);
        int adjustedCenterY = (int) (centerY * canvasHeight);

        Log.d(MainActivity.TAG,
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

    public Bitmap cropToRect(Bitmap originalImage){
        //todo: make sure this is working 100% right

        int adjustedCenterX = (int) (centerX * originalImage.getWidth());
        int adjustedCenterY = (int) (centerY * originalImage.getHeight());
        int adjustedWidth = (int) (width * originalImage.getWidth());
        int adjustedHeight = (int) (height * originalImage.getHeight());

        int cornerX = adjustedCenterX - (adjustedWidth / 2);
        int cornerY = adjustedCenterY - (adjustedHeight / 2);

        Matrix transformation = new Matrix();
        transformation.setRotate((float) angle, adjustedCenterX, adjustedCenterY);

        Bitmap rotated = Bitmap.createBitmap( //todo: optimize performance by doing 1 transformation
                originalImage,
                0,
                0,
                originalImage.getWidth(),
                originalImage.getWidth(),
                transformation,
                true
        );

        return Bitmap.createBitmap(
                rotated,
                cornerX,
                cornerY,
                adjustedWidth,
                adjustedHeight
        );
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

    @Deprecated
    public void printState(){
        Log.d(MainActivity.TAG,
                String.format(Locale.US,
                        "\ncenterX: %s \ncenterY %s\nwidth: %s\nheight: %s\nangle: %s",
                        centerX, centerY, width, height, angle));

    }

}