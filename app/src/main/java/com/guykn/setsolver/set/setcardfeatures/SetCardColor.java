package com.guykn.setsolver.set.setcardfeatures;

import com.guykn.setsolver.imageprocessing.classifiers.FeatureClassifier;

public class SetCardColor extends SetCardFeature<SetCardColor.SetCardColorEnum>{

    public  enum SetCardColorEnum implements SetCardFeatureEnum {
        PURPLE(0, "Purple"), GREEN(1, "Green"), RED(2, "Red");

        private String name;

        public String getName() {
            return name;
        }

        private int id;

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return getName();
        }

        private SetCardColorEnum(int id, String name) {
            this.id = id;
            this.name = name;
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

    public SetCardColor(FeatureClassifier.ClassificationResult result){
        this.color = getEnumFromId(result.getResultID());
        this.certainty = result.getResultProbability();
    }

    @Override
    public String toString(){
        return String.format("%.0f%% certainty that the card is %s", certainty*100, color.toString());
    }
    protected SetCardColorEnum[] getFeatureEnumValues(){
        return SetCardColorEnum.values();
    }


}
