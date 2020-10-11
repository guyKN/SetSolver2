package com.guykn.setsolver.set.setcardfeatures;

import androidx.annotation.NonNull;

import com.guykn.setsolver.imageprocessing.classifiers.FeatureClassifier;

import java.util.Locale;

//todo: make names shorter
//todo: merge more code into multiple lines
public class SetCardShape extends SetCardFeature<SetCardShape.SetCardShapeEnum> {
    public enum SetCardShapeEnum implements SetCardFeatureEnum {
            CIRCLE(0, "Circle"), DIAMOND(1, "Diamond"), S_SHAPE(2, "S-Shape");
        private String name;
        public String getName() {
            return name;
        }
        private int id;
        public int getId() {
            return id;
        }

        @Override
        @NonNull
        public String toString() {
            return getName();
        }

        private SetCardShapeEnum(int id, String name) {
            this.id = id;
            this.name = name;
        }

    }
    private SetCardShapeEnum shape;
    public SetCardShapeEnum getShape() {
        return shape;
    }

    public SetCardShape(FeatureClassifier.ClassificationResult result) {
        this.shape = getEnumFromId(result.getResultID());
        this.certainty = result.getResultProbability();
    }

    private SetCardShape(SetCardShapeEnum shape, double certainty) {
        this.shape = shape;
        this.certainty = certainty;
    }

    @Override
    @NonNull
    public String toString(){
        return String.format(Locale.US,
                "%.0f%% certainty that the shape is %s",
                certainty*100, shape.toString());
    }

    protected SetCardShapeEnum[] getFeatureEnumValues(){
        return SetCardShapeEnum.values();
    }

}
