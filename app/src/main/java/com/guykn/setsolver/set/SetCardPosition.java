package com.guykn.setsolver.set;

import android.graphics.Point;

import org.opencv.core.RotatedRect;

import java.lang.reflect.Array;

/**
 * Stores the position of an individual set Card on the camera.
 * Always stores x and y values from 0 to 1, so that its compatible with all resolutions, and no conversion errors ocour.
 */
public class SetCardPosition {
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
        Point p3 = new Point(adjustedCenterX + segment1X - segment2X,
                             adjustedCenterY + segment1Y - segment2Y);

        return new Point[] {p0,p1,p2,p3};
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