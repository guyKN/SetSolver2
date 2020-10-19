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

    private final Size originalCanvasSize; //todo: find better name
    public GenericRotatedRectangle(RotatedRect rotatedRect, Size originalCanvasSize){
        super(rotatedRect.center, rotatedRect.size, rotatedRect.angle);
        this.originalCanvasSize = originalCanvasSize;


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
