package com.guykn.setsolver.set.setcardfeatures;

import androidx.annotation.NonNull;

import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;

import java.util.Locale;

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
    final public SetCardShapeEnum shapeEnum;

    public SetCardShape(ClassificationResult result) {
        super(result.probability);
        this.shapeEnum = getEnumFromId(result.id);
    }

    private SetCardShape(SetCardShapeEnum shapeEnum, double confidence) {
        super(confidence);
        this.shapeEnum = shapeEnum;
    }

    protected SetCardShapeEnum[] getFeatureEnumValues(){
        return SetCardShapeEnum.values();
    }

    @Override
    public String getName() {
        return shapeEnum.getName();
    }

    @Override
    public String getDescription() {
        return String.format(Locale.US,
                "%.0f%% confidence that the card's shape is %s",
                confidence *100, getName());

    }

}
