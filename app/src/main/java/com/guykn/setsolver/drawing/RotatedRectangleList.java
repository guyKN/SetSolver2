package com.guykn.setsolver.drawing;

import android.util.Log;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.imageprocessing.detect.CardDetector;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.List;

/**
 * Simple list of rotatedRectangles, implements the cardAction interface, and simply adds the card's position to the list whenether the doAction method is called
 */
public class RotatedRectangleList extends DrawingCallbackList<GenericRotatedRectangle>
        implements CardDetector.CardAction, SavableToGallery {

    public RotatedRectangleList(){}

    public RotatedRectangleList(List<GenericRotatedRectangle> rectangles){
        this();
        setDrawables(rectangles);
    }

    @Override
    public void doAction(GenericRotatedRectangle position) {
        addDrawable(position);
    }

    public void printStates(){
        Log.d(GenericRotatedRectangle.TAG, "number of drawables: " + getDrawables().size());
        for(GenericRotatedRectangle rect: getDrawables()){
            rect.printState();
        }
    }

    @Override
    public void saveToGallery(ImageFileManager fileManager, Mat originalImage){
        for(GenericRotatedRectangle rectangle: getDrawables()){
            rectangle.saveToGallery(fileManager, originalImage);
        }
    }

    @Override
    public void saveToGallery(ImageFileManager fileManager, Mat originalImage, Size scaledDownSize){
        for(GenericRotatedRectangle rectangle: getDrawables()){
            rectangle.saveToGallery(fileManager, originalImage, scaledDownSize);
        }
    }

}
