package com.guykn.setsolver.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.guykn.setsolver.CameraActivity;
import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.MainActivity;
import com.guykn.setsolver.ui.main.CameraFragment;

import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;

public class GenericRotatedRectangle extends RotatedRect implements DrawableOnCanvas{
    public static final String TAG = "GenericRotatedRectangle";

<<<<<<< HEAD
    private final Size originalCanvasSize; //todo: find better name
    public GenericRotatedRectangle(RotatedRect rotatedRect, Size originalCanvasSize){
        super(rotatedRect.center, rotatedRect.size, rotatedRect.angle);
        this.originalCanvasSize = originalCanvasSize;
=======
    private double centerX;
    private double centerY;
    private double width;
    private double height;
    private double angle;

    private static int lastHeight = 0;
    private static int lastWidth = 0;

    private static final boolean writeToConsole = false;

    public void drawOnCanvas2(Canvas canvas, Paint paint) {
        //todo: make sure this works. It currently doesn't work completely and sometimes draws wierdly
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        if(writeToConsole) Log.d(TAG, "Width: " + canvasWidth + " Height  " + canvasHeight);

        float adjustableAngle = (float) angle;
        double adjustableWidth = width;
        double adjustableHeight = height;

        Log.d(TAG, angle > -45.0 ? "angle>-45":"-45>angle");
        Log.d(TAG, (height>width ? "h>w": "w>h"));
        boolean doAngleAdjustment = true;
        if(adjustableAngle<-45.0 && doAngleAdjustment){
            adjustableAngle+=90;
        }
        if(adjustableWidth>adjustableHeight && doAngleAdjustment){
            double temp = width;
            //noinspection SuspiciousNameCombination
            adjustableWidth = adjustableHeight;
            adjustableHeight = temp;
        }

        Rect rect = new Rect(
                (int) ((centerX - adjustableWidth/2)*canvasWidth),
                (int) ((centerY - adjustableHeight/2)*canvasHeight),
                (int) ((centerX + adjustableWidth/2)*canvasWidth),
                (int) ((centerY + adjustableHeight/2)*canvasHeight)
        );
        canvas.save();
        canvas.rotate(adjustableAngle, (int)(centerX*canvasWidth), (int)(centerY*canvasHeight));

        checkWH(canvasWidth, canvasHeight);
        checkWH(canvas.getWidth(), canvas.getHeight());
>>>>>>> parent of 6c8959f... Scale down images with a consistent aspect ratio


    }
    public GenericRotatedRectangle(GenericRotatedRectangle genericRotatedRect){
        super(genericRotatedRect.center, genericRotatedRect.size, genericRotatedRect.angle);
        this.originalCanvasSize = genericRotatedRect.originalCanvasSize;
    }



    @Override
    public void drawOnCanvas(Canvas canvas, Paint paint) {
        Size canvasSize = new Size(canvas.getWidth(), canvas.getHeight());

        Log.d(GenericRotatedRectangle.TAG,
                "canvas aspect ratio: " +
                        canvasSize.width/canvasSize.height
                        + "\noriginal canvas aspect ratio: " +
                        originalCanvasSize.width/originalCanvasSize.height);


        double xScaleFactor = canvasSize.width/originalCanvasSize.width;
        double yScaleFactor = canvasSize.height/originalCanvasSize.height;
        Log.d(TAG, "xScaleFactor: " + xScaleFactor + "\nyScaleFactor: " + yScaleFactor);
        canvas.save();
        canvas.rotate((float) angle, (float) center.x, (float) center.y);
        canvas.drawRect(
                (float) ( (center.x - size.width / 2) * yScaleFactor), //left
                (float) ( (center.y - size.height / 2) * yScaleFactor), //top
                (float) ( (center.y + size.width / 2) * yScaleFactor), //right
                (float) ((center.y + size.width / 2) * yScaleFactor), //bottom
                paint
        );
        canvas.restore();
    }

    @Override
    public void saveToGallery(ImageFileManager fileManager, Mat originalImage) {

    }

    public Mat cropToRect(Mat originalImage){
        //todo: actually implement
        return null;
    }
}
