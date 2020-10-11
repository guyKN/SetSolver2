package com.guykn.setsolver.imageprocessing.classifiers;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.SetCardFinder;
import com.guykn.setsolver.set.SetCard;
import com.guykn.setsolver.set.setcardfeatures.SetCardColor;
import com.guykn.setsolver.set.setcardfeatures.SetCardCount;
import com.guykn.setsolver.set.setcardfeatures.SetCardFill;
import com.guykn.setsolver.set.setcardfeatures.SetCardShape;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import java.io.IOException;

public class CardClassifier {

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

    public SetCard classify(Mat mat){
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);//todo: check if this is the proper config
        Utils.matToBitmap(mat, bmp);
        return classify(bmp);
    }
    public SetCard classify(Bitmap image){
        SetCardCount count = new SetCardCount(countClassifier.classify(image));
        SetCardColor color = new SetCardColor(colorClassifier.classify(image));
        SetCardShape shape = new SetCardShape(shapeClassifier.classify(image));
        SetCardFill fill = new SetCardFill(fillClassifier.classify(image));

        return new SetCard(pos, color, count, fill, shape);
    }

}
