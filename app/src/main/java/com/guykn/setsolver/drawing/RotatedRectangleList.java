package com.guykn.setsolver.drawing;

import android.util.Log;

import com.guykn.setsolver.imageprocessing.detect.CardDetector;

import java.util.List;

/**
 * Simple list of rotatedRectangles, implements the cardAction interface, and simply adds the card's position to the list whenether the doAction method is called
 */
public class RotatedRectangleList extends DrawableList<GenericRotatedRectangle>
        implements CardDetector.CardAction {

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
}
