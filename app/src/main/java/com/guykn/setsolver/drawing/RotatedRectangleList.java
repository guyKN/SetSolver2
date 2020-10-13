package com.guykn.setsolver.drawing;

import com.guykn.setsolver.imageprocessing.detect.CardAction;

/**
 * Simple list of rotatedRectangles, implements the cardAction interface, and simply adds the card's position to the list whenether the doAction method is called
 */
public class RotatedRectangleList extends DrawableList<GenericRotatedRectangle> implements CardAction {
    @Override
    public void doAction(GenericRotatedRectangle position) {
        addDrawable(position);
    }
}
