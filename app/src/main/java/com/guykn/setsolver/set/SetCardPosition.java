package com.guykn.setsolver.set;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;

import org.opencv.core.RotatedRect;

import java.lang.reflect.Array;

/**
 * Stores the position of an individual set Card on the camera.
 * Always stores x and y values from 0 to 1, so that its compatible with all resolutions, and no conversion errors occur.
 */
public class SetCardPosition {
    //todo: also allow to crop a bitmap to the position
    //todo: also draw on a canvas in this class.

    private double centerX;
    private double centerY;
    private double width;
    private double height;
    private double angle;

    public Point[] getCorners(int canvasWidth, int canvasHeight){
        double angleRadians = angle/180*Math.PI;
        double sin = Math.sin(angleRadians);
        double cos = Math.sin(angleRadians);

        int segment1X = (int) (width / 2 * cos) * canvasWidth;
        int segment1Y = (int) ((width / 2 * sin) * canvasHeight);

        int segment2X = (int) ((height / 2 * (-sin)) * canvasWidth);
        int segment2Y = (int) ((height / 2 * cos) * canvasHeight);

        int adjustedCenterX = (int) (centerX * canvasWidth);
        int adjustedCenterY = (int) (centerY * canvasHeight);

        Point p0 = new Point(adjustedCenterX + segment1X + segment2X,
                             adjustedCenterY + segment1Y + segment2Y);
        Point p1 = new Point(adjustedCenterX + segment1X - segment2X,
                             adjustedCenterY + segment1Y - segment2Y);
        Point p2 = new Point(adjustedCenterX - segment1X - segment2X,
                             adjustedCenterY - segment1Y - segment2Y);
        Point p3 = new Point(adjustedCenterX - segment1X + segment2X,
                             adjustedCenterY - segment1Y + segment2Y);

        return new Point[] {p0,p1,p2,p3};
    }

    public Bitmap cropToCard(Bitmap originalImage){
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

    private SetCardPosition(double centerX, double centerY, double width, double height, double angle) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.angle = angle;
    }

    public static SetCardPosition fromRotatedRect(RotatedRect rect, int canvasWidth, int canvasHeight){
        double centerX = rect.center.x / canvasWidth;
        double centerY = rect.center.x / canvasHeight;
        double width = rect.size.width/canvasWidth;
        double height = rect.size.height / canvasHeight;
        double angle = rect.angle;
        return new SetCardPosition(centerX, centerY, width,height, angle);
    }

}