package com.guykn.setsolver.set.setcardfeatures;

import com.guykn.setsolver.R;
import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;

public class SetCardColor extends SetCardFeature<SetCardColor.SetCardColorEnum>{

    public  enum SetCardColorEnum implements SetCardFeatureEnum {
        RED(0, "Red", R.color.redCard),
        PURPLE(1, "Purple", R.color.purpleCard),
        GREEN(2, "Green", R.color.greenCard);

        private final String name;

        public int getColorCode() {
            return colorCode;
        }

        private int colorCode;

        public String getName() {
            return name;
        }

        private final int id;

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return getName();
        }

        private SetCardColorEnum(int id, String name, int colorCode) {
            this.id = id;
            this.name = name;
            this.colorCode = colorCode;
        }


    }

    private SetCardColorEnum color;

    public SetCardColorEnum getColor() {
        return color;
    }

    private SetCardColor(SetCardColorEnum color, double certainty) {
        this.certainty = certainty;
        this.color = color;
    }

    public SetCardColor(ClassificationResult result){
        this.color = getEnumFromId(result.id);
        this.certainty = result.probability;
    }

    @Override
    public String toString(){
        return String.format("%.0f%% certainty that the card is %s", certainty*100, color.toString());
    }
    protected SetCardColorEnum[] getFeatureEnumValues(){
        return SetCardColorEnum.values();
    }


}
