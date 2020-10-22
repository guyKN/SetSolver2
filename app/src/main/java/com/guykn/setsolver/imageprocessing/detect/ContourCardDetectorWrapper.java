package com.guykn.setsolver.imageprocessing.detect;

import android.content.Context;

import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

import org.opencv.core.Mat;

public class ContourCardDetectorWrapper implements CardDetector{

    public ContourCardDetectorWrapper(Context context){
    }
    /**
     * Calls the doAction method on with a setCardPosition for every card found in the image
     * @param image the Image to process
     * @param config the config that describes thresholds and other settings
     * @param action implements CardAction. calls the doAction(GenericRotatedRectangle position) method on this for every card found.
     */

    @Override
    public void findAllCardsAndDoAction(Mat image, ImageProcessingConfig config, CardAction action){
        ContourBasedCardDetector cardDetector =
                new ContourBasedCardDetector(image, config);
        cardDetector.findAllCardsAndDoAction(action);
    }

    /**
     * Returns all cards on the board, as a RotatedRectangleList.
     * Usually not recommended for use if you are trying to also process or classify those rectangles. For that, use findAllCardsAndDo action
     * @return RotatedRectangleList: a list containing all rectangles on the image.
     */
    @Override
    public RotatedRectangleList getAllCardRectangles(Mat image, ImageProcessingConfig config){
        RotatedRectangleList allCardRectangles = new RotatedRectangleList();
        findAllCardsAndDoAction(image, config, allCardRectangles);
        return allCardRectangles;
    }


}
