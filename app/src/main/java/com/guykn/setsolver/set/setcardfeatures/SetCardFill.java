package com.guykn.setsolver.set.setcardfeatures;

import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;

public class SetCardFill extends SetCardFeature<SetCardFill.SetCardFillEnum> {
    public enum SetCardFillEnum implements SetCardFeatureEnum {
        EMPTY(0, "Empty"), PARTIAL(1, "Partial"), FULL(2, "Full");

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

        private SetCardFillEnum(int id, String name) {
            this.id = id;
            this.name = name;
        }

    }
    private SetCardFillEnum fill;

    public SetCardFillEnum getFill() {
        return fill;
    }


    private SetCardFill(SetCardFillEnum fill, double certainty) {
        this.fill = fill;
        this.certainty = certainty;
    }

    public SetCardFill(ClassificationResult result){
        this.fill = getEnumFromId(result.getResultID());
        this.certainty = result.getResultProbability();
    }

    @Override
    public String toString(){
        return String.format("%.0f%% certainty that the cards' fill is %s", certainty*100, fill.toString());
    }

    protected SetCardFillEnum[] getFeatureEnumValues(){
        return SetCardFillEnum.values();
    }

}
