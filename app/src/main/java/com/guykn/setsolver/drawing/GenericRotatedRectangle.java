package com.guykn.setsolver.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

import org.opencv.core.RotatedRect;

/**
 * Stores the location of a rotated rectangle in an image (Primary used to track where set cards are)
 * Is generic, meaning all values are stored as doubles from 0 to 1. This ensures that conversion between different canvases is easy.
 */
public class GenericRotatedRectangle implements DrawableOnCanvas {
    //todo: also allow to crop a bitmap to the position
    //todo: also draw on a canvas in this class.

    private double centerX;
    private double centerY;
    private double width;
    private double height;
    private double angle;


    @Override
    public void drawOnCanvas(Canvas canvas, Paint paint) {
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

    private Point[] getCorners(int canvasWidth, int canvasHeight){
        double angleRadians = angle/180*Math.PI;
        double sin = Math.sin(angleRadians);
        double cos = Math.sin(angleRadians);

        int segment1X = (int) (width / 2 * cos) * canvasWidth;
        int segment1Y = (int) ((width / 2 * sin) * canvasHeight);

        int segment2X = (int) ((height / 2 * (-sin)) * canvasWidth);
        int segment2Y = (int) ((height / 2 * cos) * canvasHeight);

        int adjustedCenterX = (int) (centerX * canvasWidth);
        int adjustedCenterY = (int) (centerY * canvasHeight);

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
        int adjustedCenterX = (int) centerX * originalImage.getWidth();
        int adjustedCenterY = (int) centerY * originalImage.getHeight();
        int adjustedWidth = (int) width * originalImage.getWidth();
        int adjustedHeight = (int) height * originalImage.getHeight();

        int cornerX = adjustedCenterX - (adjustedWidth / 2);
        int cornerY = adjustedCenterY - (adjustedHeight / 2);

        Matrix transformation = new Matrix();
        transformation.setRotate((float) angle, adjustedCenterX, adjustedCenterY);
        return Bitmap.createBitmap(
                originalImage,
                cornerX,
                cornerY,
                adjustedWidth,
                adjustedHeight,
                transformation,
                true
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
        centerX = rect.center.x / canvasWidth;
        centerY = rect.center.x / canvasHeight;
        width = rect.size.width/canvasWidth;
        height = rect.size.height / canvasHeight;
        angle = rect.angle;
    }

}