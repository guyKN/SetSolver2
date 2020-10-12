package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.detect.SetCardFinder;
import com.guykn.setsolver.set.SetCard;
import com.guykn.setsolver.set.GenericRotatedRectangle;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

import java.io.IOException;

public class CardClassifier {

    //todo: use SetCardPosition rather than RotatedRect in the functions
    //todo: maybe add factory method?
    private static String COLOR_MODEL_PATH =  "Models/Color/";
    private static String FILL_MODEL_PATH =  "Models/Fill/";
    private static String SHAPE_MODEL_PATH =  "Models/Shape/";
    private static String COUNT_MODEL_PATH = "Models/Count/";

    private Context context;
    private SetCardFinder.Config config;

    private FeatureClassifier countClassifier;
    private FeatureClassifier colorClassifier;
    private FeatureClassifier fillClassifier;
    private FeatureClassifier shapeClassifier;

    public CardClassifier(Context context, SetCardFinder.Config config) throws IOException{
        this.context = context;
        this.config = config;
        countClassifier = new FeatureClassifier(context, config, COUNT_MODEL_PATH);
        colorClassifier = new FeatureClassifier(context, config, COLOR_MODEL_PATH);
        fillClassifier = new FeatureClassifier(context, config, FILL_MODEL_PATH);
        shapeClassifier = new FeatureClassifier(context, config, SHAPE_MODEL_PATH);
    }


    public SetCard classify(Bitmap originalImage, GenericRotatedRectangle position){
        Bitmap cropped = position.cropToRect(originalImage);
        SetCardCount count = new SetCardCount(countClassifier.classify(cropped));
        SetCardColor color = new SetCardColor(colorClassifier.classify(cropped));
        SetCardShape shape = new SetCardShape(shapeClassifier.classify(cropped));
        SetCardFill fill = new SetCardFill(fillClassifier.classify(cropped));

        return position.toSetCard(color, count, fill, shape);
    }

}
