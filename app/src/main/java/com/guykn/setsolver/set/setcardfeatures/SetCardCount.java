package com.guykn.setsolver.set.setcardfeatures;

import com.guykn.setsolver.imageprocessing.classify.ClassificationResult;

public class SetCardCount extends SetCardFeature<SetCardCount.SetCardCountEnum>{
    public enum SetCardCountEnum implements SetCardFeatureEnum{
        ONE_SYMBOL(0, "1 Symbol"), TWO_SYMBOLS(1, "2 Symbols"), THREE_SYMBOLS(2, "3 Symbols");
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

        private SetCardCountEnum(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private double certainty;
    private SetCardCountEnum count;
    public SetCardCountEnum getCount() {
        return count;
    }

    private SetCardCount(SetCardCountEnum count, double certainty) {
        this.certainty = certainty;
        this.count = count;
    }
    public SetCardCount(ClassificationResult result){
        this.count = getEnumFromId(result.getResultID());
        this.certainty= result.getResultProbability();
    }

    @Override
    public String toString(){
        return String.format("%.0f%% certainty that there are %s", certainty*100, count.toString());
    }

    protected SetCardCountEnum[] getFeatureEnumValues(){
        return SetCardCountEnum.values();
    }

}
